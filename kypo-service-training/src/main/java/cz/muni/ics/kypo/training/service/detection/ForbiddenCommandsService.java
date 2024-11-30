package cz.muni.ics.kypo.training.service.detection;

import cz.muni.ics.kypo.training.persistence.model.Submission;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.detection.CheatingDetection;
import cz.muni.ics.kypo.training.persistence.model.detection.DetectedForbiddenCommand;
import cz.muni.ics.kypo.training.persistence.model.detection.DetectionEventParticipant;
import cz.muni.ics.kypo.training.persistence.model.detection.ForbiddenCommand;
import cz.muni.ics.kypo.training.persistence.model.detection.ForbiddenCommandsDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.enums.CommandType;
import cz.muni.ics.kypo.training.persistence.model.enums.DetectionEventType;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.repository.SubmissionRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.detection.DetectedForbiddenCommandRepository;
import cz.muni.ics.kypo.training.persistence.repository.detection.DetectionEventParticipantRepository;
import cz.muni.ics.kypo.training.persistence.repository.detection.ForbiddenCommandsDetectionEventRepository;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cz.muni.ics.kypo.training.utils.CheatingDetectionUtils.extractParticipant;

@Service
public class ForbiddenCommandsService {
    private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
    private final SubmissionRepository submissionRepository;
    private final ForbiddenCommandsDetectionEventRepository forbiddenCommandsDetectionEventRepository;
    private final DetectionEventParticipantRepository detectionEventParticipantRepository;
    private final DetectedForbiddenCommandRepository detectedForbiddenCommandRepository;
    private final TrainingRunRepository trainingRunRepository;
    private final TrainingRunService trainingRunService;
    private final ElasticsearchApiService elasticsearchApiService;
    private final DetectionEventService detectionEventService;

    /**
     * Instantiates a new Cheating detection service.
     *
     * @param submissionRepository                      the submission repository
     * @param forbiddenCommandsDetectionEventRepository the forbidden commands detection event repository
     * @param detectionEventParticipantRepository       the detection event participant repository
     * @param detectedForbiddenCommandRepository        the detected forbidden commands repository
     * @param trainingRunRepository                     the training run repository
     * @param trainingRunService                        the training run service
     * @param elasticsearchApiService                   the elastic search api service
     * @param detectionEventService                     the detection events service
     */
    @Autowired
    public ForbiddenCommandsService(SubmissionRepository submissionRepository,
                                    ForbiddenCommandsDetectionEventRepository forbiddenCommandsDetectionEventRepository,
                                    DetectionEventParticipantRepository detectionEventParticipantRepository,
                                    DetectedForbiddenCommandRepository detectedForbiddenCommandRepository,
                                    TrainingRunRepository trainingRunRepository,
                                    TrainingRunService trainingRunService,
                                    ElasticsearchApiService elasticsearchApiService,
                                    DetectionEventService detectionEventService) {
        this.submissionRepository = submissionRepository;
        this.forbiddenCommandsDetectionEventRepository = forbiddenCommandsDetectionEventRepository;
        this.detectionEventParticipantRepository = detectionEventParticipantRepository;
        this.detectedForbiddenCommandRepository = detectedForbiddenCommandRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.trainingRunService = trainingRunService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.detectionEventService = detectionEventService;
    }

    /**
     * finds all forbidden command events of cheating detection
     *
     * @param cheatingDetectionId the cheating detection id
     * @return list of events
     */
    public List<ForbiddenCommandsDetectionEvent> findAllForbiddenCommandsEventsOfDetection(Long cheatingDetectionId) {
        return forbiddenCommandsDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    }

    /**
     * finds forbidden command event by id
     *
     * @param eventId the event id
     * @return the event
     */
    public ForbiddenCommandsDetectionEvent findForbiddenCommandsEventById(Long eventId) {
        return forbiddenCommandsDetectionEventRepository.findForbiddenCommandsEventById(eventId);
    }

    /**
     * Executes a cheating detection of type FORBIDDEN_COMMANDS
     *
     * @param cd the cheating detection
     */
    void executeCheatingDetectionOfForbiddenCommands(CheatingDetection cd) {
        for (var run : trainingRunService.findAllByTrainingInstanceId(cd.getTrainingInstanceId())) {
            executeForbiddenCommandsMethodForRun(cd, run);
        }

    }

    private void executeForbiddenCommandsMethodForRun(CheatingDetection cd, TrainingRun run) {
        List<Submission> submissions;
        submissions = submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
        if (submissions.isEmpty()) {
            return;
        }
        for (int i = 0; i < submissions.size() + 1; i++) {
            evaluateForbiddenCommandsForSubmission(cd, run, submissions, i);
        }
    }

    private void evaluateForbiddenCommandsForSubmission(CheatingDetection cd, TrainingRun run, List<Submission> submissions, int submissionIndex) {
        LocalDateTime from;
        LocalDateTime to;
        Submission currentSubmission;
        if (submissionIndex == submissions.size()) {
            currentSubmission = submissions.get(submissionIndex - 1);
            if (run.getState() == TRState.RUNNING) {
                from = currentSubmission.getDate();
                to = LocalDateTime.now();
            } else {
                return;
            }
        } else {
            currentSubmission = submissions.get(submissionIndex);
            from = (submissionIndex == 0) ? run.getStartTime() : submissions.get(submissionIndex - 1).getDate();
            to = currentSubmission.getDate();
        }
        generateForbiddenCommandEvent(cd, run, from, to, currentSubmission);
    }

    private void generateForbiddenCommandEvent(CheatingDetection cd, TrainingRun run, LocalDateTime from, LocalDateTime to, Submission currentSubmission) {
        List<DetectedForbiddenCommand> forbiddenCommands = evaluateForbiddenCommands(cd.getCommands(), getSubmittedCommandsFromRunInInterval(run, from, to));
        if (!forbiddenCommands.isEmpty()) {
            DetectionEventParticipant participant = extractParticipant(currentSubmission, detectionEventService.getUserFullName(currentSubmission));
            auditForbiddenCommandsEvent(currentSubmission, cd, participant, forbiddenCommands);
        }
    }

    private List<Map<String, Object>> getSubmittedCommandsFromRunInInterval(TrainingRun run, LocalDateTime from, LocalDateTime to) {
        List<Map<String, Object>> submittedCommands;
        submittedCommands = elasticsearchApiService.findAllConsoleCommandsBySandboxAndTimeRange(
                run.getSandboxInstanceRefId(),
                from.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
                to.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
        return submittedCommands;
    }

    private List<DetectedForbiddenCommand> evaluateForbiddenCommands(List<ForbiddenCommand> forbiddenCommands, List<Map<String, Object>> submittedCommands) {
        List<DetectedForbiddenCommand> commandsList = new ArrayList<>();
        for (var commandMap : submittedCommands) {
            if (commandMapContainsNull(commandMap)) continue;
            String command = commandMap.get("cmd").toString();
            LocalDateTime localDateTime = Instant
                    .parse(commandMap.get("timestamp_str").toString())
                    .atZone(ZoneId.of("UTC")).toLocalDateTime();

            for (var forbiddenCommand : forbiddenCommands) {
                detectForbiddenCommands(commandsList, commandMap, command, localDateTime, forbiddenCommand);
            }
        }
        return commandsList;
    }

    private static void detectForbiddenCommands(List<DetectedForbiddenCommand> commandsList, Map<String, Object> commandMap, String command, LocalDateTime localDateTime, ForbiddenCommand forbiddenCommand) {
        String type = forbiddenCommand.getType() == CommandType.BASH ? "bash-command" : "msf-command";
        if (commandMap.get("cmd_type").toString().equals(type)
                && command != null
                && command.contains(forbiddenCommand.getCommand())) {
            DetectedForbiddenCommand detectedCommand = new DetectedForbiddenCommand();
            detectedCommand.setCommand(command);
            detectedCommand.setType(forbiddenCommand.getType());
            detectedCommand.setHostname(commandMap.get("hostname").toString());
            detectedCommand.setOccurredAt(localDateTime);
            commandsList.add(detectedCommand);
        }
    }

    private static boolean commandMapContainsNull(Map<String, Object> commandMap) {
        return commandMap == null ||
                commandMap.get("cmd") == null ||
                commandMap.get("cmd_type") == null ||
                commandMap.get("hostname") == null;
    }

    private void auditForbiddenCommandsEvent(Submission submission, CheatingDetection cd, DetectionEventParticipant participant,
                                             List<DetectedForbiddenCommand> detectedForbiddenCommands) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        ForbiddenCommandsDetectionEvent event = new ForbiddenCommandsDetectionEvent();
        event.setCommonDetectionEventParameters(submission, cd, DetectionEventType.FORBIDDEN_COMMANDS, 1);
        event.setCommandCount(detectedForbiddenCommands.size());
        event.setTrainingRunId(submission.getTrainingRun().getId());
        event.setParticipants(participant.getParticipantName());
        Long eventId = forbiddenCommandsDetectionEventRepository.save(event).getId();
        participant.setDetectionEventId(eventId);
        detectedForbiddenCommands.forEach(command -> {
            command.setDetectionEventId(eventId);
            detectedForbiddenCommandRepository.save(command);
        });
        participant.setCheatingDetectionId(cd.getId());
        detectionEventParticipantRepository.save(participant);
    }
}
