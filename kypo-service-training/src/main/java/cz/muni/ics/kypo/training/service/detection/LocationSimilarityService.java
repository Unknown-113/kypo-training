package cz.muni.ics.kypo.training.service.detection;

import cz.muni.ics.kypo.training.persistence.model.Submission;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.detection.CheatingDetection;
import cz.muni.ics.kypo.training.persistence.model.detection.DetectionEventParticipant;
import cz.muni.ics.kypo.training.persistence.model.detection.LocationSimilarityDetectionEvent;
import cz.muni.ics.kypo.training.persistence.model.enums.DetectionEventType;
import cz.muni.ics.kypo.training.persistence.repository.SubmissionRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingLevelRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import cz.muni.ics.kypo.training.persistence.repository.detection.LocationSimilarityDetectionEventRepository;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cz.muni.ics.kypo.training.utils.CheatingDetectionUtils.checkIfContainsParticipant;
import static cz.muni.ics.kypo.training.utils.CheatingDetectionUtils.extractParticipant;
import static cz.muni.ics.kypo.training.utils.CheatingDetectionUtils.generateParticipantString;

@Service
public class LocationSimilarityService {
    private static final Logger LOG = LoggerFactory.getLogger(CheatingDetectionService.class);
    private final TrainingLevelRepository trainingLevelRepository;
    private final SubmissionRepository submissionRepository;
    private final LocationSimilarityDetectionEventRepository locationSimilarityDetectionEventRepository;
    private final TrainingRunRepository trainingRunRepository;
    private final TrainingRunService trainingRunService;
    private final TrainingInstanceService trainingInstanceService;
    private final DetectionEventService detectionEventService;
    @Autowired
    Environment environment;

    /**
     * Instantiates a new Cheating detection service.
     *
     * @param trainingLevelRepository                    the training level repository
     * @param submissionRepository                       the submission repository
     * @param locationSimilarityDetectionEventRepository the location similarity detection event repository
     * @param trainingRunRepository                      the training run repository
     * @param trainingRunService                         the training run service
     * @param trainingInstanceService                    the training instance service
     */
    @Autowired
    public LocationSimilarityService(TrainingLevelRepository trainingLevelRepository,
                                     SubmissionRepository submissionRepository,
                                     LocationSimilarityDetectionEventRepository locationSimilarityDetectionEventRepository,
                                     TrainingRunRepository trainingRunRepository,
                                     TrainingRunService trainingRunService,
                                     TrainingInstanceService trainingInstanceService,
                                     DetectionEventService detectionEventService) {
        this.trainingLevelRepository = trainingLevelRepository;
        this.submissionRepository = submissionRepository;
        this.locationSimilarityDetectionEventRepository = locationSimilarityDetectionEventRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.trainingRunService = trainingRunService;
        this.trainingInstanceService = trainingInstanceService;
        this.detectionEventService = detectionEventService;
    }

    /**
     * find all location similarity events of cheating detection
     *
     * @param cheatingDetectionId the cheating detection id
     * @return list of events
     */
    public List<LocationSimilarityDetectionEvent> findAllLocationSimilarityEventsOfDetection(Long cheatingDetectionId) {
        return locationSimilarityDetectionEventRepository.findAllByCheatingDetectionId(cheatingDetectionId);
    }

    /**
     * find location similarity event by id
     *
     * @param eventId the event id
     * @return event
     */
    public LocationSimilarityDetectionEvent findLocationSimilarityEventById(Long eventId) {
        return locationSimilarityDetectionEventRepository.findLocationSimilarityEventById(eventId);
    }

    /**
     * Executes a cheating detection of type LOCATION_SIMILARITY
     *
     * @param cd the training instance id
     */
    void executeCheatingDetectionOfLocationSimilarity(CheatingDetection cd) {
        Long trainingInstanceId = cd.getTrainingInstanceId();
        trainingLevelRepository
                .findAllByTrainingDefinitionId(trainingInstanceService.findById(trainingInstanceId).getTrainingDefinition().getId())
                .stream()
                .map(level -> submissionRepository.getSubmissionsByLevelAndInstance(trainingInstanceId, level.getId()))
                .forEach(submissions -> evaluateLocationSimilarityByLevels(submissions, cd));
    }

    private void evaluateLocationSimilarityByLevels(List<Submission> submissions, CheatingDetection cd) {

        List<List<Submission>> groups = new ArrayList<>();
        generateLocationSimilarityGroups(submissions, groups);
        for (var group : groups) {
            generateEventFromGroup(cd, group);
        }
    }

    private void generateEventFromGroup(CheatingDetection cd, List<Submission> group) {
        List<Long> runIds;
        Set<DetectionEventParticipant> participants;
        if (group.size() < 2) {
            return;
        }
        participants = new HashSet<>();
        runIds = new ArrayList<>();
        for (var submission : group) {
            Long submissionRunId = submission.getTrainingRun().getId();
            if (!runIds.contains(submissionRunId)) {
                DetectionEventParticipant participant = extractParticipant(submission, detectionEventService.getUserFullName(submission));
                if (!checkIfContainsParticipant(participants, participant)) {
                    participants.add(participant);
                }
                runIds.add(submissionRunId);
            }
            trainingRunService.auditRunHasDetectionEvent(submission.getTrainingRun());
        }
        if (participants.size() > 1) {
            auditLocationSimilarityEvent(group.get(0), cd, participants);
        }
    }

    private void generateLocationSimilarityGroups(List<Submission> submissions, List<List<Submission>> groups) {
        boolean hasSimilarIPToExistingGroup;
        for (var submission : submissions) {
            hasSimilarIPToExistingGroup = false;
            for (var group : groups) {
                if (checkLocationSimilarity(group.get(0).getIpAddress(), submission.getIpAddress())) {
                    group.add(submission);
                    hasSimilarIPToExistingGroup = true;
                }
            }
            if (hasSimilarIPToExistingGroup || submissions.size() == 1) {
                continue;
            }
            groups.add(new ArrayList<>() {{
                add(submission);
            }});
        }
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

    private void auditLocationSimilarityEvent(Submission submission, CheatingDetection cd, Set<DetectionEventParticipant> participants) {
        TrainingRun run = submission.getTrainingRun();
        run.setHasDetectionEvent(true);
        trainingRunRepository.save(run);
        LocationSimilarityDetectionEvent event = new LocationSimilarityDetectionEvent();
        event.setCommonDetectionEventParameters(submission, cd, DetectionEventType.LOCATION_SIMILARITY, participants.size());
        extractLocationSimilaritySpecificInfo(submission, event);
        event.setParticipants(generateParticipantString(participants));
        detectionEventService.saveParticipants(participants, locationSimilarityDetectionEventRepository.save(event).getId(), cd.getId());
    }

    private void extractLocationSimilaritySpecificInfo(Submission submission, LocationSimilarityDetectionEvent event) {
        String submissionDomainName;
        try {
            InetAddress envAddress = InetAddress.getByName(environment.getProperty("server.address"));
            submissionDomainName = InetAddress.getByName(submission.getIpAddress()).getHostName();
            event.setAddressDeploy(envAddress.getHostName().equals(submissionDomainName));
        } catch (UnknownHostException e) {
            submissionDomainName = "unspecified";
            event.setAddressDeploy(false);
        }
        event.setDns(submissionDomainName);
        event.setIpAddress(submission.getIpAddress());
    }
}
