package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.ics.kypo.training.api.responses.VariantAnswer;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.detection.*;
import cz.muni.ics.kypo.training.persistence.model.enums.CheatingDetectionState;
import cz.muni.ics.kypo.training.persistence.model.enums.CommandType;
import cz.muni.ics.kypo.training.persistence.model.enums.DetectionEventType;
import cz.muni.ics.kypo.training.persistence.repository.SubmissionRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingLevelRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.detection.*;
import cz.muni.ics.kypo.training.service.api.AnswersStorageApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.*;
import java.util.*;

/**
 * The type Cheating detection service.
 */
@Service
public class CheatingDetectionService {

    private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
    private final AbstractDetectionEventRepository detectionEventRepository;
    private final CheatingDetectionRepository cheatingDetectionRepository;
    private final TrainingLevelRepository trainingLevelRepository;
    private final SubmissionRepository submissionRepository;
    private final AnswerSimilarityDetectionEventRepository answerSimilarityDetectionEventRepository;
    private final LocationSimilarityDetectionEventRepository locationSimilarityDetectionEventRepository;
    private final TimeProximityDetectionEventRepository timeProximityDetectionEventRepository;
    private final MinimalSolveTimeDetectionEventRepository minimalSolveTimeDetectionEventRepository;
    private final NoCommandsDetectionEventRepository noCommandsDetectionEventRepository;
    private final ForbiddenCommandsDetectionEventRepository forbiddenCommandsDetectionEventRepository;
    private final DetectionEventParticipantRepository detectionEventParticipantRepository;
    private final DetectedForbiddenCommandRepository detectedForbiddenCommandRepository;
    private final TrainingRunRepository trainingRunRepository;
    private final AnswersStorageApiService answersStorageApiService;
    private final TrainingRunService trainingRunService;
    private final TrainingInstanceService trainingInstanceService;
    private final ElasticsearchApiService elasticsearchApiService;
    private final UserService userService;
    @Autowired
    Environment environment;

    /**
     * Instantiates a new Cheating detection service.
     *
     * @param abstractDetectionEventRepository           the cheat repository
     * @param cheatingDetectionRepository                the cheating detection repository
     * @param trainingLevelRepository                    the training level repository
     * @param submissionRepository                       the submission repository
     * @param answerSimilarityDetectionEventRepository   the answer similarity detection event repository
     * @param locationSimilarityDetectionEventRepository the location similarity detection event repository
     * @param timeProximityDetectionEventRepository      the time proximity detection event repository
     * @param minimalSolveTimeDetectionEventRepository   the minimal solve time detection event repository
     * @param noCommandsDetectionEventRepository         the no commands detection event repository
     * @param forbiddenCommandsDetectionEventRepository  the forbidden commands detection event repository
     * @param detectionEventParticipantRepository        the detection event participant repository
     * @param detectedForbiddenCommandRepository         the detected forbidden commands repository
     * @param trainingRunRepository                      the training run repository
     * @param answersStorageApiService                   the answers storage api service
     * @param trainingRunService                         the training run service
     * @param trainingInstanceService                    the training instance service
     * @param elasticsearchApiService                    the elastic search api service
     * @param userService                                the user service
     */
    @Autowired
    public CheatingDetectionService(AbstractDetectionEventRepository abstractDetectionEventRepository,
                                    CheatingDetectionRepository cheatingDetectionRepository,
                                    TrainingLevelRepository trainingLevelRepository,
                                    SubmissionRepository submissionRepository,
                                    AnswerSimilarityDetectionEventRepository answerSimilarityDetectionEventRepository,
                                    LocationSimilarityDetectionEventRepository locationSimilarityDetectionEventRepository,
                                    TimeProximityDetectionEventRepository timeProximityDetectionEventRepository,
                                    MinimalSolveTimeDetectionEventRepository minimalSolveTimeDetectionEventRepository,
                                    NoCommandsDetectionEventRepository noCommandsDetectionEventRepository,
                                    ForbiddenCommandsDetectionEventRepository forbiddenCommandsDetectionEventRepository,
                                    DetectionEventParticipantRepository detectionEventParticipantRepository,
                                    DetectedForbiddenCommandRepository detectedForbiddenCommandRepository,
                                    TrainingRunRepository trainingRunRepository,
                                    AnswersStorageApiService answersStorageApiService,
                                    TrainingRunService trainingRunService,
                                    ElasticsearchApiService elasticsearchApiService,
                                    TrainingInstanceService trainingInstanceService,
                                    UserService userService) {
        this.detectionEventRepository = abstractDetectionEventRepository;
        this.cheatingDetectionRepository = cheatingDetectionRepository;
        this.trainingLevelRepository = trainingLevelRepository;
        this.submissionRepository = submissionRepository;
        this.answerSimilarityDetectionEventRepository = answerSimilarityDetectionEventRepository;
        this.locationSimilarityDetectionEventRepository = locationSimilarityDetectionEventRepository;
        this.timeProximityDetectionEventRepository = timeProximityDetectionEventRepository;
        this.minimalSolveTimeDetectionEventRepository = minimalSolveTimeDetectionEventRepository;
        this.noCommandsDetectionEventRepository = noCommandsDetectionEventRepository;
        this.forbiddenCommandsDetectionEventRepository = forbiddenCommandsDetectionEventRepository;
        this.detectionEventParticipantRepository = detectionEventParticipantRepository;
        this.detectedForbiddenCommandRepository = detectedForbiddenCommandRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.answersStorageApiService = answersStorageApiService;
        this.trainingRunService = trainingRunService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.trainingInstanceService = trainingInstanceService;
        this.userService = userService;
    }

    public void createCheatingDetection(CheatingDetection cheatingDetection) {
        cheatingDetection.setExecutedBy(userService.getUserRefFromUserAndGroup().getUserRefFullName());
        cheatingDetection.setResults(0L);
        cheatingDetection.setExecuteTime(LocalDateTime.now());
        cheatingDetectionRepository.save(cheatingDetection);
    }

    public void executeCheatingDetection(CheatingDetection cd) {
        cd.setCurrentState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        if (cd.getAnswerSimilarityState() == CheatingDetectionState.QUEUED) {
            executeCheatingDetectionOfAnswerSimilarity(cd);
        }
        if (cd.getLocationSimilarityState() == CheatingDetectionState.QUEUED) {
            executeCheatingDetectionOfLocationSimilarity(cd);
        }
        if (cd.getTimeProximityState() == CheatingDetectionState.QUEUED) {
            if (cd.getProximityThreshold() == null) {
                cd.setProximityThreshold(120L);
            }
            executeCheatingDetectionOfTimeProximity(cd);
        }
        if (cd.getMinimalSolveTimeState() == CheatingDetectionState.QUEUED) {
            executeCheatingDetectionOfMinimalSolveTime(cd);
        }
        if (cd.getNoCommandsState() == CheatingDetectionState.QUEUED) {
            executeCheatingDetectionOfNoCommands(cd);
        }
        if (cd.getForbiddenCommandsState() == CheatingDetectionState.QUEUED) {
            executeCheatingDetectionOfForbiddenCommands(cd);
        }
        cd.setResults(detectionEventRepository.getNumberOfDetections(cd.getId()));
        cd.setCurrentState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private void executeCheatingDetectionOfAnswerSimilarity(CheatingDetection cd) {
        cd.setAnswerSimilarityState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        Set<TrainingRun> runs = trainingRunService.findAllByTrainingInstanceId(trainingInstanceId);
        Map<String, List<VariantAnswer>> answerMap = new HashMap<>();
        List<TrainingLevel> trainingLevels = trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService
                        .findById(trainingInstanceId).getTrainingDefinition().getId());
        Map<Long, TrainingLevel> trainingLevelsById = new HashMap<>();
        String sandboxId;
        for (var run : runs) {
            sandboxId = run.getSandboxInstanceRefId();
            answerMap.put(sandboxId, answersStorageApiService.getAnswersBySandboxId(sandboxId).getVariantAnswers());
        }
        for (var level : trainingLevels) {
            trainingLevelsById.put(level.getId(), level);
        }
        for (Submission submission : submissionRepository.getIncorrectSubmissionsOfTrainingInstance(trainingInstanceId)) {
            Long currentId = submission.getLevel().getId();
            if (checkIfAnswerBelongsToDifferentLevel(answerMap.get(submission.getTrainingRun().getSandboxInstanceRefId()), submission.getProvided())) {
                continue;
            }
            if (!trainingLevelsById.containsKey(currentId)) {
                continue;
            }
            for (TrainingRun run : runs) {
                evalCheatOfAnswerSimilarity(run, submission, answerMap.get(run.getSandboxInstanceRefId()),
                        trainingLevelsById.get(currentId).getAnswerVariableName(), cd);
            }
        }
        cd.setAnswerSimilarityState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private boolean checkIfAnswerBelongsToDifferentLevel(List<VariantAnswer> answers, String provided) {
        return answers.stream()
                .map(VariantAnswer::getAnswerContent)
                .anyMatch(provided::equals);
    }

    private void evalCheatOfAnswerSimilarity(TrainingRun run, Submission submission, List<VariantAnswer> answers,
                                             String answerVariable, CheatingDetection cd) {
        String submissionSandboxId = submission.getTrainingRun().getSandboxInstanceRefId();
        String sandboxId = run.getSandboxInstanceRefId();
        Set<DetectionEventParticipant> participants = new HashSet<>();
        if (sandboxId.equals(submissionSandboxId)) {
            return;
        }
        for (var answer : answers) {
            if (answer.getAnswerContent().equals(submission.getProvided()) && answerVariable.equals(answer.getAnswerVariableName())) {
                DetectionEventParticipant participant = extractParticipant(submission);
                if (!checkIfContainsParticipant(participants, participant)) {
                    participants.add(participant);
                }
            }
        }
        if (!participants.isEmpty()) {
            String answerOwner = userService.getUserRefDTOByUserRefId(run.getParticipantRef().getUserRefId()).getUserRefFullName();
            List<Submission> ownerSubmissions = submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
            for (var ownerSubmission : ownerSubmissions) {
                if (Objects.equals(ownerSubmission.getLevel().getId(), submission.getLevel().getId())) {
                    participants.add(extractParticipant(ownerSubmission));
                }
            }
            auditAnswerSimilarityEvent(submission, cd, participants, answerOwner);
            run.setHasDetectionEvent(true);
            trainingRunRepository.save(run);
        }
    }

    /**
     * Executes a cheating detection of type LOCATION_SIMILARITY
     *
     * @param cd the training instance id
     */
    private void executeCheatingDetectionOfLocationSimilarity(CheatingDetection cd) {
        cd.setLocationSimilarityState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<TrainingLevel> trainingLevels = trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId());
        for (var level : trainingLevels) {
            evaluateLocationSimilarityByLevels(submissionRepository.getSubmissionsByLevelAndInstance(trainingInstanceId, level.getId()), cd);
        }
        cd.setLocationSimilarityState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private void evaluateLocationSimilarityByLevels(List<Submission> submissions, CheatingDetection cd) {
        boolean wasPut;
        List<List<Submission>> groups = new ArrayList<>();
        Set<DetectionEventParticipant> participants;
        List<Long> runIds;
        for (var submission : submissions) {
            wasPut = false;
            for (var group : groups) {
                if (checkLocationSimilarity(group.get(0).getIpAddress(), submission.getIpAddress())) {
                    group.add(submission);
                    wasPut = true;
                }
            }
            if (wasPut || submissions.size() == 1) {
                continue;
            }
            groups.add(new ArrayList<>() {{
                add(submission);
            }});
        }
        for (var group : groups) {
            if (group.size() < 2) {
                continue;
            }
            participants = new HashSet<>();
            runIds = new ArrayList<>();
            for (var submission : group) {
                Long submissionRunId = submission.getTrainingRun().getId();
                if (!runIds.contains(submissionRunId)) {
                    DetectionEventParticipant participant = extractParticipant(submission);
                    if (!checkIfContainsParticipant(participants, participant)) {
                        participants.add(participant);
                    }
                    runIds.add(submissionRunId);
                }
                auditRunDetectionEvent(submission.getTrainingRun());
            }
            if (participants.size() > 1) {
                auditLocationSimilarityEvent(group.get(0), cd, participants);
            }
        }
    }

    private void executeCheatingDetectionOfTimeProximity(CheatingDetection cd) {
        cd.setTimeProximityState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<TrainingLevel> trainingLevels = trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId());
        List<Submission> submissions;
        List<Submission> detectedGroup = new ArrayList<>();
        Set<DetectionEventParticipant> participants = new HashSet<>();

        for (var level : trainingLevels) {
            submissions = submissionRepository.getAllTimeProximitySubmissionsOfLevel(trainingInstanceId, level.getId());
            for (int i = 1; i < submissions.size(); i++) {
                int j = i - 1;
                var first = submissions.get(j);
                var second = submissions.get(i);
                long timeProximity = Duration.between(first.getDate(), second.getDate()).toSeconds();
                if (timeProximity < cd.getProximityThreshold()) {
                    if (detectedGroup.isEmpty()) {
                        detectedGroup.add(first);
                    }
                    detectedGroup.add(second);
                } else {
                    for (var sub : detectedGroup) participants.add(extractParticipant(sub));
                    if (!detectedGroup.isEmpty()) {
                        auditTimeProximityEvent(detectedGroup.get(0), cd, participants);
                        detectedGroup.clear();
                        participants.clear();
                    }
                }
            }
            for (var sub : detectedGroup) {
                DetectionEventParticipant participant = extractParticipant(sub);
                if (!checkIfContainsParticipant(participants, participant)) {
                    participants.add(participant);
                }
                auditRunDetectionEvent(sub.getTrainingRun());
            }
            if (!detectedGroup.isEmpty() && participants.size() > 1) {
                auditTimeProximityEvent(detectedGroup.get(0), cd, participants);
                detectedGroup.clear();
                participants.clear();
            }
        }

        cd.setTimeProximityState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private void auditRunDetectionEvent(TrainingRun run) {
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
    }

    private boolean checkIfContainsParticipant(Set<DetectionEventParticipant> participants, DetectionEventParticipant participant) {
        return participants.stream()
                .anyMatch(elem -> elem.getUserId().equals(participant.getUserId()));
    }

    private void executeCheatingDetectionOfMinimalSolveTime(CheatingDetection cd) {
        cd.setMinimalSolveTimeState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<Submission> submissions = submissionRepository.getCorrectSubmissionsOfTrainingInstance(trainingInstanceId);
        Map<Long, List<Submission>> detectedByLevel = new HashMap<>();
        Map<Long, Long> submissionTimes = new HashMap<>();
        Set<DetectionEventParticipant> participants;
        boolean newParticipant = true;
        LocalDateTime levelStart;
        LocalDateTime levelEnd;
        Submission current;
        Submission previous = new Submission();
        for (Submission submission : submissions) {
            current = submission;
            if (current.getLevel().getMinimalPossibleSolveTime() != null) {
                if (newParticipant) {
                    levelStart = current.getTrainingRun().getStartTime();
                    newParticipant = false;

                } else {
                    if (current.getTrainingRun().equals(previous.getTrainingRun())) {
                        levelStart = previous.getDate();
                    } else {
                        levelStart = current.getTrainingRun().getStartTime();
                    }
                }
                levelEnd = current.getDate();
                long levelDuration = Duration.between(levelStart, levelEnd).toSeconds();
                long minimalSolveTime = current.getLevel().getMinimalPossibleSolveTime() * 60;

                if (levelDuration < minimalSolveTime) {
                    var arr = detectedByLevel.get(current.getLevel().getId());
                    if (arr == null) {
                        arr = new ArrayList<>();
                    }
                    arr.add(current);
                    detectedByLevel.put(current.getLevel().getId(), arr);
                    submissionTimes.put(current.getId(), levelDuration);
                }

            }
            previous = current;
        }
        for (var set : detectedByLevel.entrySet()) {
            participants = new HashSet<>();
            for (var sub : set.getValue()) {
                auditRunDetectionEvent(sub.getTrainingRun());
                participants.add(extractParticipant(sub, true, submissionTimes.get(sub.getId())));
            }
            Submission s = set.getValue().get(0);
            auditMinimalSolveTimeEvent(s, cd, participants, s.getLevel().getMinimalPossibleSolveTime() * 60);
        }
        cd.setMinimalSolveTimeState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private boolean wasSolutionDisplayed(List<AbstractAuditPOJO> events, Submission submission) {
        for (AbstractAuditPOJO event : events) {
            if (event.getLevel() == submission.getLevel().getId() && event.getType().contains("SolutionDisplayed")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executes a cheating detection of type NO_COMMANDS
     *
     * @param cd the training instance id
     */
    private void executeCheatingDetectionOfNoCommands(CheatingDetection cd) {
        cd.setNoCommandsState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<TrainingRun> trainingRuns = new ArrayList<>(trainingRunService.findAllByTrainingInstanceId(trainingInstanceId));
        List<TrainingLevel> trainingLevels = trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId());
        Map<Long, TrainingLevel> trainingLevelsById = new HashMap<>();
        Map<Long, List<Submission>> detectedSubmissionsByLevels = new HashMap<>();
        for (var level : trainingLevels) {
            trainingLevelsById.put(level.getId(), level);
        }
        List<Submission> submissions = new ArrayList<>();
        LocalDateTime from;
        Submission submission;
        for (var run : trainingRuns) {
            List<AbstractAuditPOJO> events = elasticsearchApiService.findAllEventsFromTrainingRun(run);
            submissions = submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
            for (int i = 0; i < submissions.size() - 1; i++) {
                submission = submissions.get(i);
                from = (i == 0) ? run.getStartTime() : submissions.get(i - 1).getDate();
                Long currentId = submission.getLevel().getId();
                if (!trainingLevelsById.containsKey(currentId) || !trainingLevelsById.get(currentId).isCommandsRequired()) {
                    continue;
                }
                if (wasSolutionDisplayed(events, submission)) {
                    continue;
                }
                if (evalCheatOfNoCommands(run.getSandboxInstanceRefId(), from, submission)) {
                    if (detectedSubmissionsByLevels.containsKey(submission.getLevel().getId())) {
                        var tempSubmissions = detectedSubmissionsByLevels.get(submission.getLevel().getId());
                        tempSubmissions.add(submission);
                        detectedSubmissionsByLevels.put(submission.getLevel().getId(), tempSubmissions);
                    } else {
                        List<Submission> subs = new ArrayList<>();
                        subs.add(submission);
                        detectedSubmissionsByLevels.put(submission.getLevel().getId(), subs);
                    }
                }
            }
        }
        Set<DetectionEventParticipant> runs;
        for (var set : detectedSubmissionsByLevels.entrySet()) {
            runs = new HashSet<>();
            for (var sub : set.getValue()) {
                runs.add(extractParticipant(sub));
                auditRunDetectionEvent(sub.getTrainingRun());
            }
            auditNoCommandsEvent(set.getValue().get(0), cd, runs);
        }
        cd.setNoCommandsState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private boolean evalCheatOfNoCommands(String sandboxId, LocalDateTime from, Submission submission) {
        long fromMilli = from.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        long toMilli = submission.getDate().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        return elasticsearchApiService.findAllConsoleCommandsBySandboxAndTimeRange(sandboxId, fromMilli, toMilli).isEmpty();
    }

    private DetectionEventParticipant extractParticipant(Submission s) {
        return extractParticipant(s, false, 0L);
    }

    private DetectionEventParticipant extractParticipant(Submission s, boolean isMinimal, Long solvedInTime) {
        DetectionEventParticipant participant = new DetectionEventParticipant();
        participant.setIpAddress(s.getIpAddress());
        participant.setUserId(s.getTrainingRun().getParticipantRef().getUserRefId());
        participant.setOccurredAt(s.getDate());
        participant.setParticipantName(userService.getUserRefDTOByUserRefId(s.getTrainingRun().getParticipantRef().getUserRefId()).getUserRefFullName());
        if (isMinimal) {
            participant.setSolvedInTime(solvedInTime);
        }
        return participant;
    }

    /**
     * Executes a cheating detection of type FORBIDDEN_COMMANDS
     *
     * @param cd the cheating detection
     */
    private void executeCheatingDetectionOfForbiddenCommands(CheatingDetection cd) {
        cd.setForbiddenCommandsState(CheatingDetectionState.RUNNING);
        updateCheatingDetection(cd);
        Long trainingInstanceId = cd.getTrainingInstanceId();
        List<Submission> submissions;
        List<Map<String, Object>> submittedCommands;
        LocalDateTime from;
        Submission currentSubmission;
        List<DetectedForbiddenCommand> forbiddenCommands;

        for (var run : trainingRunService.findAllByTrainingInstanceId(trainingInstanceId)) {
            submissions = submissionRepository.getCorrectSubmissionsOfTrainingRunSorted(run.getId());
            for (int i = 0; i < submissions.size(); i++) {
                currentSubmission = submissions.get(i);
                from = (i == 0) ? run.getStartTime() : submissions.get(i - 1).getDate();
                submittedCommands = elasticsearchApiService.findAllConsoleCommandsBySandboxAndTimeRange(
                        run.getSandboxInstanceRefId(),
                        from.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
                        currentSubmission.getDate().atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
                forbiddenCommands = evaluateForbiddenCommands(cd.getForbiddenCommands(), submittedCommands);
                if (!forbiddenCommands.isEmpty()) {
                    auditForbiddenCommandsEvent(currentSubmission, cd, extractParticipant(currentSubmission), forbiddenCommands);
                }
            }
        }
        cd.setForbiddenCommandsState(CheatingDetectionState.FINISHED);
        updateCheatingDetection(cd);
    }

    private List<DetectedForbiddenCommand> evaluateForbiddenCommands(List<ForbiddenCommand> fc, List<Map<String, Object>> submittedCommands) {
        List<DetectedForbiddenCommand> commandsList = new ArrayList<>();
        for (var commandMap : submittedCommands) {
            String command = commandMap.get("cmd").toString();
            String type = commandMap.get("cmd_type").toString();
            String hostname = commandMap.get("hostname").toString();

            Instant instant = Instant.parse(commandMap.get("timestamp_str").toString());
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));

            LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

            for (var forbiddenCommand : fc) {
                String forbiddenType = forbiddenCommand.getType() == CommandType.BASH ? "bash-command" : "msf-command";
                if (type.equals(forbiddenType) && command != null && command.contains(forbiddenCommand.getCommand())) {
                    DetectedForbiddenCommand detectedCommand = new DetectedForbiddenCommand();
                    detectedCommand.setCommand(command);
                    detectedCommand.setType(forbiddenCommand.getType());
                    detectedCommand.setHostname(hostname);
                    detectedCommand.setOccurredAt(localDateTime);
                    commandsList.add(detectedCommand);
                }
            }
        }
        return commandsList;
    }

    public void deleteDetectionEvents(Long cheatingDetectionId) {
        detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
    }

    public void deleteCheatingDetection(Long cheatingDetectionId, Long trainingInstanceId) {
        List<TrainingRun> trainingRuns = new ArrayList<>(trainingRunService.findAllByTrainingInstanceId(trainingInstanceId));
        for (var run : trainingRuns) {
            run.setHasDetectionEvent(false);
            trainingRunRepository.save(run);
        }
        detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cheatingDetectionId);
        cheatingDetectionRepository.deleteCheatingDetectionById(cheatingDetectionId);
    }

    public void deleteAllCheatingDetectionsOfTrainingInstance(Long trainingInstanceId) {
        detectionEventRepository.deleteDetectionEventsOfTrainingInstance(trainingInstanceId);
        cheatingDetectionRepository.deleteAllCheatingDetectionsOfTrainingInstance(trainingInstanceId);
    }

    public CheatingDetection findCheatingDetectionById(Long cheatingDetectionId) {
        return cheatingDetectionRepository.findCheatingDetectionById(cheatingDetectionId);
    }

    public void rerunCheatingDetection(Long cheatingDetectionId) {
        CheatingDetection cd = cheatingDetectionRepository.findCheatingDetectionById(cheatingDetectionId);
        cd.setCurrentState(CheatingDetectionState.RUNNING);
        if (cd.getAnswerSimilarityState() != CheatingDetectionState.DISABLED) {
            cd.setAnswerSimilarityState(CheatingDetectionState.QUEUED);
        }
        if (cd.getLocationSimilarityState() != CheatingDetectionState.DISABLED) {
            cd.setLocationSimilarityState(CheatingDetectionState.QUEUED);
        }
        if (cd.getTimeProximityState() != CheatingDetectionState.DISABLED) {
            cd.setTimeProximityState(CheatingDetectionState.QUEUED);
        }
        if (cd.getMinimalSolveTimeState() != CheatingDetectionState.DISABLED) {
            cd.setMinimalSolveTimeState(CheatingDetectionState.QUEUED);
        }
        if (cd.getNoCommandsState() != CheatingDetectionState.DISABLED) {
            cd.setNoCommandsState(CheatingDetectionState.QUEUED);
        }
        if (cd.getForbiddenCommandsState() != CheatingDetectionState.DISABLED) {
            cd.setForbiddenCommandsState(CheatingDetectionState.QUEUED);
        }
        detectionEventRepository.deleteDetectionEventsOfCheatingDetection(cd.getId());
        cd.setExecuteTime(LocalDateTime.now());
        cd.setResults(0L);
        cheatingDetectionRepository.save(cd);
        executeCheatingDetection(cd);
    }

    public List<AnswerSimilarityDetectionEvent> findAllAnswerSimilarityEventsOfDetection(Long cheatingDetectionId) {
        return answerSimilarityDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    }

    public List<LocationSimilarityDetectionEvent> findAllLocationSimilarityEventsOfDetection(Long cheatingDetectionId) {
        return locationSimilarityDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    }

    public List<TimeProximityDetectionEvent> findAllTimeProximityEventsOfDetection(Long cheatingDetectionId) {
        return timeProximityDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    }

    public List<MinimalSolveTimeDetectionEvent> findAllMinimalSolveTimeEventsOfDetection(Long cheatingDetectionId) {
        return minimalSolveTimeDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    }

    public List<NoCommandsDetectionEvent> findAllNoCommandsEventsOfDetection(Long cheatingDetectionId) {
        return noCommandsDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    }

    public List<ForbiddenCommandsDetectionEvent> findAllForbiddenCommandsEventsOfDetection(Long cheatingDetectionId) {
        return forbiddenCommandsDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    }

    public Page<AbstractDetectionEvent> findAllDetectionEventsOfCheatingDetection(Long cheatingDetectionId, Pageable pageable, Predicate predicate) {
        return detectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId, pageable, predicate);
    }

    public Page<DetectionEventParticipant> findAllParticipantsOfEvent(Long eventId, Pageable pageable) {
        return detectionEventParticipantRepository.findAllByEventId(eventId, pageable);
    }

    public List<DetectionEventParticipant> findAllParticipantsOfEvent(Long eventId) {
        return detectionEventParticipantRepository.findAllByEventId(eventId);
    }

    public List<DetectionEventParticipant> findAllParticipantsOfCheatingDetection(Long cheatingDetectionId) {
        return detectionEventParticipantRepository.findAllParticipantsOfCheatingDetection(cheatingDetectionId);
    }

    public Page<DetectedForbiddenCommand> findAllForbiddenCommandsOfDetectionEvent(Long eventId, Pageable pageable) {
        return detectedForbiddenCommandRepository.findAllByEventId(eventId, pageable);
    }

    public List<DetectedForbiddenCommand> findAllForbiddenCommandsOfDetectionEvent(Long eventId) {
        return detectedForbiddenCommandRepository.findAllByEventId(eventId);
    }

    public AbstractDetectionEvent findDetectionEventById(Long eventId) {
        return detectionEventRepository.findDetectionEventById(eventId);
    }

    public AnswerSimilarityDetectionEvent findAnswerSimilarityEventById(Long eventId) {
        return answerSimilarityDetectionEventRepository.findAnswerSimilarityEventById(eventId);
    }

    public LocationSimilarityDetectionEvent findLocationSimilarityEventById(Long eventId) {
        return locationSimilarityDetectionEventRepository.findLocationSimilarityEventById(eventId);
    }

    public TimeProximityDetectionEvent findTimeProximityEventById(Long eventId) {
        return timeProximityDetectionEventRepository.findTimeProximityEventById(eventId);
    }

    public MinimalSolveTimeDetectionEvent findMinimalSolveTimeEventById(Long eventId) {
        return minimalSolveTimeDetectionEventRepository.findMinimalSolveTimeEventById(eventId);
    }

    public NoCommandsDetectionEvent findNoCommandsEventById(Long eventId) {
        return noCommandsDetectionEventRepository.findNoCommandsEventById(eventId);
    }

    public ForbiddenCommandsDetectionEvent findForbiddenCommandsEventById(Long eventId) {
        return forbiddenCommandsDetectionEventRepository.findForbiddenCommandsEventById(eventId);
    }

    public Page<CheatingDetection> findAllCheatingDetectionsOfTrainingInstance(Long trainingInstanceId, Pageable pageable) {
        return cheatingDetectionRepository.findAllByTrainingInstanceId(trainingInstanceId, pageable);
    }

    private boolean checkLocationSimilarity(String ip, String otherIp) {
        if (ip != null && otherIp != null) {
            try {
                InetAddress firstIp = InetAddress.getByName(ip);
                InetAddress secondIp = InetAddress.getByName(otherIp);
                return firstIp.equals(secondIp);
            } catch (UnknownHostException e) {
                return ip.equals(otherIp);
            }
        }
        return false;
    }

    private void auditAnswerSimilarityEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants, String answerOwner) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        AnswerSimilarityDetectionEvent event = new AnswerSimilarityDetectionEvent();
        event.setCommonDetectionEventParameters(submission, cd, DetectionEventType.ANSWER_SIMILARITY, participants.size());
        event.setAnswer(submission.getProvided());
        event.setAnswerOwner(answerOwner);
        event.setParticipants(generateParticipantString(participants));
        answerSimilarityDetectionEventRepository.save(event);
        saveParticipants(participants, event.getId(), cd.getId());
    }

    private void auditLocationSimilarityEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        LocationSimilarityDetectionEvent event = new LocationSimilarityDetectionEvent();
        event.setCommonDetectionEventParameters(submission, cd, DetectionEventType.LOCATION_SIMILARITY, participants.size());
        String submissionDomainName;
        try {
            InetAddress envAddress = InetAddress.getByName(environment.getProperty("server.address"));
            submissionDomainName = InetAddress.getByName(submission.getIpAddress()).getHostName();
            event.setIsAddressDeploy(envAddress.getHostName().equals(submissionDomainName));
        } catch (UnknownHostException e) {
            submissionDomainName = "unspecified";
            event.setIsAddressDeploy(false);
        }
        event.setDns(submissionDomainName);
        event.setIpAddress(submission.getIpAddress());
        event.setParticipants(generateParticipantString(participants));
        saveParticipants(participants, locationSimilarityDetectionEventRepository.save(event).getId(), cd.getId());
    }

    private void auditMinimalSolveTimeEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants,
                                            Long minimalSolveTime) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        MinimalSolveTimeDetectionEvent event = new MinimalSolveTimeDetectionEvent();
        event.setCommonDetectionEventParameters(submission, cd, DetectionEventType.MINIMAL_SOLVE_TIME, participants.size());
        event.setMinimalSolveTime(minimalSolveTime);
        event.setParticipants(generateParticipantString(participants));
        saveParticipants(participants, minimalSolveTimeDetectionEventRepository.save(event).getId(), cd.getId());
    }

    private void auditTimeProximityEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        TimeProximityDetectionEvent event = new TimeProximityDetectionEvent();
        event.setCommonDetectionEventParameters(submission, cd, DetectionEventType.TIME_PROXIMITY, participants.size());
        event.setThreshold(cd.getProximityThreshold());
        event.setParticipants(generateParticipantString(participants));
        saveParticipants(participants, timeProximityDetectionEventRepository.save(event).getId(), cd.getId());
    }

    private void auditNoCommandsEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        NoCommandsDetectionEvent event = new NoCommandsDetectionEvent();
        event.setCommonDetectionEventParameters(submission, cd, DetectionEventType.NO_COMMANDS, participants.size());
        event.setParticipants(generateParticipantString(participants));
        saveParticipants(participants, noCommandsDetectionEventRepository.save(event).getId(), cd.getId());
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
        for (var command : detectedForbiddenCommands) {
            command.setDetectionEventId(eventId);
            detectedForbiddenCommandRepository.save(command);
        }
        participant.setCheatingDetectionId(cd.getId());
        detectionEventParticipantRepository.save(participant);
    }

    private String generateParticipantString(Set<DetectionEventParticipant> participants) {
        StringBuilder participantString = new StringBuilder();
        for (var participant : participants) {
            participantString.append(',').append(' ').append(participant.getParticipantName());
        }
        participantString.delete(0, 2);
        return participantString.toString();
    }

    private void saveParticipants(Set<DetectionEventParticipant> participants, Long eventId, Long cheatingDetectionId) {
        for (var participant : participants) {
            participant.setDetectionEventId(eventId);
            participant.setCheatingDetectionId(cheatingDetectionId);
            detectionEventParticipantRepository.save(participant);
        }
    }

    private void updateCheatingDetection(CheatingDetection cd) {
        cheatingDetectionRepository.save(cd);
    }
}

