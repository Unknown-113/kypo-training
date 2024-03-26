package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.cheatingdetection.*;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.detection.*;
import cz.muni.ics.kypo.training.persistence.model.enums.DetectionEventType;
import cz.muni.ics.kypo.training.persistence.repository.detection.AbstractDetectionEventRepository;
import cz.muni.ics.kypo.training.service.*;
import cz.muni.ics.kypo.training.utils.AbstractFileExtensions;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The type Cheating Detection facade.
 */
@Service
@Transactional
public class CheatingDetectionFacade {

    private static final String DETECTION_EVENTS_FOLDER = "detection_events";
    private static final String ANSWER_SIMILARITY_FOLDER = "answer_similarity";
    private static final String LOCATION_SIMILARITY_FOLDER = "location_similarity";
    private static final String TIME_PROXIMITY_FOLDER = "time_proximity";
    private static final String MINIMAL_SOLVE_TIME_FOLDER = "minimal_solve_time";
    private static final String NO_COMMANDS_FOLDER = "no_commands";
    private static final String FORBIDDEN_COMMANDS_FOLDER = "forbidden_commands";
    private static final String PARTICIPANT_RESPONSE_FOLDER = "participant_groups";
    private final CheatingDetectionService cheatingDetectionService;
    public final UserService userService;
    private final TrainingInstanceService trainingInstanceService;
    private final TrainingDefinitionService trainingDefinitionService;
    private final DetectionEventMapper detectionEventMapper;
    private final CheatingDetectionMapper cheatingDetectionMapper;
    private final DetectionEventParticipantMapper detectionEventParticipantMapper;

    private final DetectedForbiddenCommandMapper detectedForbiddenCommandMapper;
    private final SecurityService securityService;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Instantiates a new Cheating detection facade.
     *
     * @param cheatingDetectionService the cheating detection service
     * @param userService              the user service
     * @param trainingInstanceService  the training instance service
     * @param detectionEventMapper     the cheating detection mapper
     * @param cheatingDetectionMapper  the cheating detection mapper
     * @param forbiddenCommandMapper   the forbidden command mapper
     * @param securityService          the security service
     */
    @Autowired
    public CheatingDetectionFacade(CheatingDetectionService cheatingDetectionService,
                                   UserService userService,
                                   TrainingInstanceService trainingInstanceService,
                                   TrainingDefinitionService trainingDefinitionService,
                                   DetectionEventMapper detectionEventMapper,
                                   CheatingDetectionMapper cheatingDetectionMapper,
                                   DetectionEventParticipantMapper detectionEventParticipantMapper,
                                   DetectedForbiddenCommandMapper forbiddenCommandMapper,
                                   SecurityService securityService,
                                   ObjectMapper objectMapper) {
        this.cheatingDetectionService = cheatingDetectionService;
        this.userService = userService;
        this.trainingInstanceService = trainingInstanceService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.detectionEventMapper = detectionEventMapper;
        this.cheatingDetectionMapper = cheatingDetectionMapper;
        this.detectionEventParticipantMapper = detectionEventParticipantMapper;
        this.detectedForbiddenCommandMapper = forbiddenCommandMapper;
        this.securityService = securityService;
        this.objectMapper = objectMapper;
    }


    /**
     * Create a new cheating detection and execute it.
     *
     * @param cheatingDetectionDTO object with constructor information
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#cheatingDetectionDTO.getTrainingInstanceId())")
    @TransactionalWO
    public void createAndExecute(CheatingDetectionDTO cheatingDetectionDTO) {
        CheatingDetection cd = this.cheatingDetectionMapper.mapToEntity(cheatingDetectionDTO);
        this.cheatingDetectionService.createCheatingDetection(cd);
        this.cheatingDetectionService.executeCheatingDetection(cd);
    }

    /**
     * Rerun cheating detection
     *
     * @param cheatingDetectionId id of cheating detection for rerun.
     * @param trainingInstanceId  id of training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public void rerunCheatingDetection(Long cheatingDetectionId, Long trainingInstanceId) {
        this.cheatingDetectionService.deleteDetectionEvents(cheatingDetectionId);
        this.cheatingDetectionService.rerunCheatingDetection(cheatingDetectionId);
    }

    /**
     * Deletes cheating detection and all its associated events.
     *
     * @param cheatingDetectionId id of cheating detection.
     * @param trainingInstanceId  id of training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public void deleteCheatingDetection(Long cheatingDetectionId, Long trainingInstanceId) {
        this.cheatingDetectionService.deleteCheatingDetection(cheatingDetectionId, trainingInstanceId);
    }

    /**
     * Finds all detection events of a cheating detection.
     *
     * @param cheatingDetectionId the cheating detection ID
     * @param trainingInstanceId  id of training instance.
     * @param pageable            the pageable
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public PageResultResource<AbstractDetectionEventDTO> findAllDetectionEventsOfCheatingDetection(Long cheatingDetectionId,
                                                                                                   Long trainingInstanceId,
                                                                                                   Pageable pageable,
                                                                                                   Predicate predicate) {
        return detectionEventMapper.mapToPageResultResource(
                this.cheatingDetectionService.findAllDetectionEventsOfCheatingDetection(cheatingDetectionId, pageable, predicate));
    }

    /**
     * Finds all participants of detection event.
     *
     * @param eventId  the detection event ID
     * @param pageable the pageable
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public PageResultResource<DetectionEventParticipantDTO> findAllParticipantsOfDetectionEvent(Long eventId,
                                                                                                Pageable pageable) {
        return detectionEventParticipantMapper.mapToPageResultResource(
                this.cheatingDetectionService.findAllParticipantsOfEvent(eventId, pageable));
    }

    /**
     * Finds all forbidden commands of detection event.
     *
     * @param eventId  the detection event ID
     * @param pageable the pageable
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public PageResultResource<DetectedForbiddenCommandDTO> findAllForbiddenCommandsOfDetectionEvent(Long eventId,
                                                                                                    Pageable pageable) {
        return detectedForbiddenCommandMapper.mapToPageResultResource(
                this.cheatingDetectionService.findAllForbiddenCommandsOfDetectionEvent(eventId, pageable));
    }

    /**
     * Finds all forbidden commands of detection event for visualization.
     *
     * @param eventId  the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public List<DetectedForbiddenCommandDTO> findAllForbiddenCommandsOfDetectionEvent(Long eventId) {
        return detectedForbiddenCommandMapper.mapToListDTO(this.cheatingDetectionService.findAllForbiddenCommandsOfDetectionEvent(eventId));
    }

    /**
     * Find detection event by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public AbstractDetectionEventDTO findDetectionEventById(Long eventId) {
        return detectionEventMapper.mapToDTO(this.cheatingDetectionService.findDetectionEventById(eventId));
    }

    /**
     * Find detection event of type answer similarity by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public AnswerSimilarityDetectionEventDTO findAnswerSimilarityEventById(Long eventId) {
        return detectionEventMapper.mapToAnswerSimilarityDetectionEventDTO(this.cheatingDetectionService.findAnswerSimilarityEventById(eventId));
    }

    /**
     * Find detection event of type location similarity by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public LocationSimilarityDetectionEventDTO findLocationSimilarityEventById(Long eventId) {
        return detectionEventMapper.mapToLocationSimilarityDetectionEventDTO(this.cheatingDetectionService.findLocationSimilarityEventById(eventId));
    }

    /**
     * Find detection event of type time proximity by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public TimeProximityDetectionEventDTO findTimeProximityEventById(Long eventId) {
        return detectionEventMapper.mapToTimeProximityDetectionEventDTO(this.cheatingDetectionService.findTimeProximityEventById(eventId));
    }

    /**
     * Find detection event of type minimal solve time by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public MinimalSolveTimeDetectionEventDTO findMinimalSolveTimeEventById(Long eventId) {
        return detectionEventMapper.mapToMinimalSolveTimeDetectionEventDTO(this.cheatingDetectionService.findMinimalSolveTimeEventById(eventId));
    }

    /**
     * Find detection event of type no commands by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public NoCommandsDetectionEventDTO findNoCommandsEventById(Long eventId) {
        return detectionEventMapper.mapToNoCommandsDetectionEventDTO(this.cheatingDetectionService.findNoCommandsEventById(eventId));
    }

    /**
     * Find detection event of type forbidden commands by its ID.
     *
     * @param eventId the detection event ID
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalWO
    public ForbiddenCommandsDetectionEventDTO findForbiddenCommandsEventById(Long eventId) {
        return detectionEventMapper.mapToForbiddenCommandsDetectionEventDTO(this.cheatingDetectionService.findForbiddenCommandsEventById(eventId));
    }

    /**
     * Exports Cheating Detection to file
     *
     * @param cheatingDetectionId the id of the cheating detection to be exported
     * @return the file containing cheating detection, {@link FileToReturnDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalRO
    public FileToReturnDTO archiveCheatingDetectionResults(Long cheatingDetectionId) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            CheatingDetection cheatingDetection = cheatingDetectionService.findCheatingDetectionById(cheatingDetectionId);
            CheatingDetectionDTO cheatingDetectionDTO = cheatingDetectionMapper.mapToDTO(cheatingDetection);

            writeCheatingDetection(zos, cheatingDetectionId, cheatingDetectionDTO);
            writeAnswerSimilarityDetectionEvents(zos, cheatingDetectionId);
            writeLocationSimilarityDetectionEvents(zos, cheatingDetectionId);
            writeTimeProximityDetectionEvents(zos, cheatingDetectionId);
            writeMinimalSolveTimeDetectionEvents(zos, cheatingDetectionId);
            writeNoCommandsDetectionEvents(zos, cheatingDetectionId);
            writeForbiddenCommandsDetectionEvents(zos, cheatingDetectionId);
            writeTraineeParticipantGroups(zos, cheatingDetectionId);

            zos.closeEntry();
            zos.close();
            FileToReturnDTO fileToReturnDTO = new FileToReturnDTO();
            fileToReturnDTO.setContent(baos.toByteArray());
            fileToReturnDTO.setTitle("cheating-detection-" + cheatingDetection.getId().toString());
            return fileToReturnDTO;
        } catch (IOException ex) {
            throw new InternalServerErrorException("The .zip file was not created since there were some processing error.", ex);
        }
    }

    /**
     * Find all cheating detections of a training instance
     *
     * @param trainingInstanceId id of Training instance for cheating detection.
     * @param pageable           pageable parameter with information about pagination.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public PageResultResource<CheatingDetectionDTO> findAllCheatingDetectionsOfTrainingInstance(Long trainingInstanceId, Pageable pageable) {

        return cheatingDetectionMapper.mapToPageResultResource(
                this.cheatingDetectionService.findAllCheatingDetectionsOfTrainingInstance(trainingInstanceId, pageable));
    }

    private void writeTraineeParticipantGroups(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<DetectionEventParticipant> participants = cheatingDetectionService
                .findAllParticipantsOfCheatingDetection(cheatingDetectionId)
                .stream()
                .filter(participant -> {
                    var participantEventType = cheatingDetectionService
                            .findDetectionEventById(participant.getDetectionEventId())
                            .getDetectionEventType();
                    return participantEventType != DetectionEventType.MINIMAL_SOLVE_TIME;
                })
                .collect(Collectors.toList());
        Map<Long, Set<Long>> eventsByParticipants = populateParticipantGroups(participants);
        Map<List<Long>, Set<Long>> participantGroups = createParticipantGroups(eventsByParticipants);

        for (Map.Entry<List<Long>, Set<Long>> entry : participantGroups.entrySet()) {
            List<Long> userGroup = entry.getKey();
            Set<Long> eventGroup = entry.getValue();

            StringBuilder usersString = new StringBuilder();
            for (var userId : userGroup) {
                usersString.append(userId).append('_');
            }
            if (usersString.length() > 0) {
                usersString.deleteCharAt(usersString.length() - 1);
            }
            ZipEntry participantResponseEntry = new ZipEntry(PARTICIPANT_RESPONSE_FOLDER + "/" + usersString + AbstractFileExtensions.CSV_FILE_EXTENSION);
            zos.putNextEntry(participantResponseEntry);
            auditParticipants(userGroup, zos);
            auditParticipantGroupEvents(eventGroup, zos);
        }
    }

    private Map<List<Long>, Set<Long>> createParticipantGroups(Map<Long, Set<Long>> userEventMap) {
        Map<List<Long>, Set<Long>> participantGroups = new HashMap<>();
        Set<Long> visitedUsers = new HashSet<>();

        for (Map.Entry<Long, Set<Long>> entry : userEventMap.entrySet()) {
            Long userId = entry.getKey();
            if (!visitedUsers.contains(userId)) {
                List<Long> userGroup = new ArrayList<>();
                Set<Long> eventGroup = new HashSet<>();
                dfs(userEventMap, userId, visitedUsers, userGroup, eventGroup);
                participantGroups.put(userGroup, eventGroup);
            }
        }

        return participantGroups;
    }

    private void dfs(Map<Long, Set<Long>> userEventMap, Long userId, Set<Long> visitedUsers,
                     List<Long> userGroup, Set<Long> eventGroup) {
        visitedUsers.add(userId);
        userGroup.add(userId);

        Set<Long> events = userEventMap.get(userId);
        if (events != null) {
            eventGroup.addAll(events);
            for (Long eventId : events) {
                for (Map.Entry<Long, Set<Long>> entry : userEventMap.entrySet()) {
                    Long nextUserId = entry.getKey();
                    if (!visitedUsers.contains(nextUserId) && entry.getValue().contains(eventId)) {
                        dfs(userEventMap, nextUserId, visitedUsers, userGroup, eventGroup);
                    }
                }
            }
        }
    }

    private Map<Long, Set<Long>> populateParticipantGroups(List<DetectionEventParticipant> participants) {
        Map<Long, Set<Long>> userEventMap = new HashMap<>();

        for (DetectionEventParticipant participant : participants) {
            Long userId = participant.getUserId();
            Long eventId = participant.getDetectionEventId();

            // Retrieve or create a set of event IDs associated with the user ID
            Set<Long> eventIds = userEventMap.getOrDefault(userId, new HashSet<>());
            eventIds.add(eventId);
            userEventMap.put(userId, eventIds);
        }
        return userEventMap;
    }

    private void auditParticipants(List<Long> userIds, ZipOutputStream zos) throws IOException {
        StringBuilder csvData = new StringBuilder();

        csvData.append("GROUP PARTICIPANTS\n");
        csvData.append("user id,name,ref\n");

        for (var userId : userIds) {
            UserRefDTO user = userService.getUserRefDTOByUserRefId(userId);
            csvData.append(String.format("%s,%s,%s\n", userId, user.getUserRefFullName(), user.getUserRefSub()));
        }
        csvData.append("\n\nEVENTS\n\n");
        byte[] bytes = csvData.toString().getBytes();
        zos.write(bytes, 0, bytes.length);
    }

    private void auditParticipantGroupEvents(Set<Long> eventIds, ZipOutputStream zos) throws IOException {
        var first = true;
        for (var eventId : eventIds) {
            AbstractDetectionEvent event = cheatingDetectionService.findDetectionEventById(eventId);
            if (first) {
                writeDetectedAt(event, zos);
                first = false;
            }
            var eventType = event.getDetectionEventType();
            List<DetectionEventParticipant> participants = cheatingDetectionService.findAllParticipantsOfEvent(eventId);
            switch (eventType) {
                case ANSWER_SIMILARITY ->
                        auditAnswerSimilarityGroup(participants, cheatingDetectionService.findAnswerSimilarityEventById(eventId), zos);
                case LOCATION_SIMILARITY ->
                        auditLocationSimilarityGroup(participants, cheatingDetectionService.findLocationSimilarityEventById(eventId), zos);
                case MINIMAL_SOLVE_TIME ->
                        auditMinimalSolveTimeGroup(participants, cheatingDetectionService.findMinimalSolveTimeEventById(eventId), zos);
                case TIME_PROXIMITY ->
                        auditTimeProximityGroup(participants, cheatingDetectionService.findTimeProximityEventById(eventId), zos);
                case NO_COMMANDS ->
                        auditNoCommandsGroup(participants, cheatingDetectionService.findNoCommandsEventById(eventId), zos);
                case FORBIDDEN_COMMANDS ->
                        auditForbiddenCommandsGroup(participants, cheatingDetectionService.findForbiddenCommandsEventById(eventId), zos);
            }
        }
    }

    private void writeDetectedAt(AbstractDetectionEvent event, ZipOutputStream zos) throws IOException {
        StringBuilder csvData = new StringBuilder();
        csvData.append(String.format("detected at:,%s\n", event.getDetectedAt().format(formatter)));
        byte[] bytes = csvData.toString().getBytes();
        zos.write(bytes, 0, bytes.length);
    }

    private void auditAnswerSimilarityGroup(List<DetectionEventParticipant> participants, AnswerSimilarityDetectionEvent event, ZipOutputStream zos) throws IOException {
        StringBuilder csvData = new StringBuilder();

        csvData.append("\nANSWER SIMILARITY EVENT\n");
        csvData.append("level order,level title,answer,answer owner\n");
        int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
        csvData.append(String.format("%s,%s,%s,%s\n", order, event.getLevelTitle(), event.getAnswer(), event.getAnswerOwner()));

        if (participants.size() > 1) {
            csvData.append("\nPARTICIPANTS\n");
            csvData.append("participant,time\n");
            for (var participant : participants) {
                csvData.append(String.format("%s,%s\n", participant.getParticipantName(), participant.getOccurredAt().format(formatter)));
            }
        }
        csvData.append("\n\n\n");
        byte[] bytes = csvData.toString().getBytes();
        zos.write(bytes, 0, bytes.length);
    }

    private void auditLocationSimilarityGroup(List<DetectionEventParticipant> participants, LocationSimilarityDetectionEvent event, ZipOutputStream zos) throws IOException {
        StringBuilder csvData = new StringBuilder();

        csvData.append("\nLOCATION SIMILARITY EVENT\n");
        csvData.append("level order,level title,hostname,IP\n");
        int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
        csvData.append(String.format("%s,%s,%s,%s\n", order, event.getLevelTitle(), event.getDns(), event.getIpAddress()));

        if (participants.size() > 1) {
            csvData.append("\nPARTICIPANTS\n");
            csvData.append("participant,time,IP\n");
            for (var participant : participants) {
                csvData.append(String.format("%s,%s,%s\n", participant.getParticipantName(), participant.getOccurredAt().format(formatter), participant.getIpAddress()));
            }
        }
        csvData.append("\n\n\n");
        byte[] bytes = csvData.toString().getBytes();
        zos.write(bytes, 0, bytes.length);
    }

    private void auditMinimalSolveTimeGroup(List<DetectionEventParticipant> participants, MinimalSolveTimeDetectionEvent event, ZipOutputStream zos) throws IOException {
        StringBuilder csvData = new StringBuilder();

        csvData.append("\nMINIMAL SOLVE TIME EVENT\n");
        csvData.append("level order,level title,minimal solve time\n");
        int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
        csvData.append(String.format("%s,%s,%s\n", order, event.getLevelTitle(), event.getMinimalSolveTime()));

        if (participants.size() > 1) {
            csvData.append("\nPARTICIPANTS\n");
            csvData.append("participant,time,solved in (seconds)\n");
            for (var participant : participants) {
                csvData.append(String.format("%s,%s,%s\n", participant.getParticipantName(), participant.getOccurredAt().format(formatter), participant.getSolvedInTime()));
            }
        }
        csvData.append("\n\n\n");
        byte[] bytes = csvData.toString().getBytes();
        zos.write(bytes, 0, bytes.length);
    }

    private void auditTimeProximityGroup(List<DetectionEventParticipant> participants, TimeProximityDetectionEvent event, ZipOutputStream zos) throws IOException {
        StringBuilder csvData = new StringBuilder();

        csvData.append("\nTIME PROXIMITY EVENT\n");
        csvData.append("level order,level title,proximity\n");
        int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
        csvData.append(String.format("%s,%s,%s\n", order, event.getLevelTitle(), event.getThreshold()));

        if (participants.size() > 1) {
            csvData.append("\nPARTICIPANTS\n");
            csvData.append("participant,time\n");
            for (var participant : participants) {
                csvData.append(String.format("%s,%s\n", participant.getParticipantName(), participant.getOccurredAt().format(formatter)));
            }
        }
        csvData.append("\n\n\n");
        byte[] bytes = csvData.toString().getBytes();
        zos.write(bytes, 0, bytes.length);
    }

    private void auditNoCommandsGroup(List<DetectionEventParticipant> participants, NoCommandsDetectionEvent event, ZipOutputStream zos) throws IOException {
        StringBuilder csvData = new StringBuilder();

        csvData.append("\nNO COMMANDS EVENT\n");
        csvData.append("level order,level title\n");
        int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
        csvData.append(String.format("%s,%s\n", order, event.getLevelTitle()));

        if (participants.size() > 1) {
            csvData.append("\nPARTICIPANTS\n");
            csvData.append("participant,time\n");
            for (var participant : participants) {
                csvData.append(String.format("%s,%s\n", participant.getParticipantName(), participant.getOccurredAt().format(formatter)));
            }
        }
        csvData.append("\n\n\n");
        byte[] bytes = csvData.toString().getBytes();
        zos.write(bytes, 0, bytes.length);
    }

    private void auditForbiddenCommandsGroup(List<DetectionEventParticipant> participants, ForbiddenCommandsDetectionEvent event, ZipOutputStream zos) throws IOException {
        StringBuilder csvData = new StringBuilder();

        csvData.append("\nFORBIDDEN COMMANDS EVENT\n");
        csvData.append("level order,level title\n");
        int order = trainingDefinitionService.findLevelById(event.getLevelId()).getOrder();
        csvData.append(String.format("%s,%s\n", order, event.getLevelTitle()));

        if (participants.size() > 1) {
            csvData.append("\nPARTICIPANTS\n");
            csvData.append("participant,time\n");
            for (var participant : participants) {
                csvData.append(String.format("%s,%s\n", participant.getParticipantName(), participant.getOccurredAt().format(formatter)));
            }
        }
        csvData.append("\nFORBIDDEN COMMANDS\n");
        csvData.append("command,type,hostname,time\n");
        List<DetectedForbiddenCommand> commands = cheatingDetectionService.findAllForbiddenCommandsOfDetectionEvent(event.getId());
        for (var command : commands) {
            csvData.append(String.format("%s,%s,%s,%s\n", command.getCommand(), command.getType(), command.getHostname(), command.getOccurredAt().format(formatter)));
        }
        csvData.append("\n\n\n");
        byte[] bytes = csvData.toString().getBytes();
        zos.write(bytes, 0, bytes.length);
    }

    private void writeCheatingDetection(ZipOutputStream zos, Long cheatingDetectionId, CheatingDetectionDTO cheatingDetectionDTO) throws IOException {
        ZipEntry cheatingDetectionEntry = new ZipEntry("cheating-detection-id" + cheatingDetectionId + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(cheatingDetectionEntry);
        zos.write(objectMapper.writeValueAsBytes(cheatingDetectionDTO));
    }

    private void writeDetectionEventToFile(ZipOutputStream zos, AbstractDetectionEvent event, String dirName) throws IOException {
        ZipEntry detectionEventEntry = new ZipEntry(DETECTION_EVENTS_FOLDER + "/" + dirName + "/detection-event-id" + event.getId() + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(detectionEventEntry);
        List<DetectionEventParticipant> participants = cheatingDetectionService.findAllParticipantsOfEvent(event.getId());
        zos.write(objectMapper.writeValueAsBytes(event));
        ZipEntry detectionEventParticipantsEntry = new ZipEntry(DETECTION_EVENTS_FOLDER + "/" + dirName + "/detection-event-id" + event.getId() + "-participants" + AbstractFileExtensions.JSON_FILE_EXTENSION);
        zos.putNextEntry(detectionEventParticipantsEntry);
        zos.write(objectMapper.writeValueAsBytes(participants));
    }

    private void writeAnswerSimilarityDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<AnswerSimilarityDetectionEvent> detectionEventsOfAS = cheatingDetectionService.findAllAnswerSimilarityEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfAS) {
            writeDetectionEventToFile(zos, event, ANSWER_SIMILARITY_FOLDER);
        }
    }

    private void writeLocationSimilarityDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<LocationSimilarityDetectionEvent> detectionEventsOfLS = cheatingDetectionService.findAllLocationSimilarityEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfLS) {
            writeDetectionEventToFile(zos, event, LOCATION_SIMILARITY_FOLDER);
        }
    }

    private void writeTimeProximityDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<TimeProximityDetectionEvent> detectionEventsOfTP = cheatingDetectionService.findAllTimeProximityEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfTP) {
            writeDetectionEventToFile(zos, event, TIME_PROXIMITY_FOLDER);
        }
    }

    private void writeMinimalSolveTimeDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<MinimalSolveTimeDetectionEvent> detectionEventsOfMST = cheatingDetectionService.findAllMinimalSolveTimeEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfMST) {
            writeDetectionEventToFile(zos, event, MINIMAL_SOLVE_TIME_FOLDER);
        }
    }

    private void writeNoCommandsDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<NoCommandsDetectionEvent> detectionEventsOfNC = cheatingDetectionService.findAllNoCommandsEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfNC) {
            writeDetectionEventToFile(zos, event, NO_COMMANDS_FOLDER);
        }
    }

    private void writeForbiddenCommandsDetectionEvents(ZipOutputStream zos, Long cheatingDetectionId) throws IOException {
        List<ForbiddenCommandsDetectionEvent> detectionEventsOfFC = cheatingDetectionService.findAllForbiddenCommandsEventsOfDetection(cheatingDetectionId);
        for (var event : detectionEventsOfFC) {
            writeDetectionEventToFile(zos, event, FORBIDDEN_COMMANDS_FOLDER);
        }
    }
}

