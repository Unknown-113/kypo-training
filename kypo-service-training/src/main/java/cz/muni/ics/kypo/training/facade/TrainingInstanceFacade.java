package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.*;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingInstanceMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapper;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.*;
import cz.muni.ics.kypo.training.service.api.TrainingFeedbackApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.api.SandboxApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Training instance facade.
 */
@Service
public class TrainingInstanceFacade {

    private final TrainingInstanceService trainingInstanceService;
    private final TrainingDefinitionService trainingDefinitionService;
    private final TrainingRunService trainingRunService;
    private final CheatingDetectionService cheatingDetectionService;
    private final UserService userService;
    private final SecurityService securityService;
    private final TrainingInstanceMapper trainingInstanceMapper;
    private final TrainingRunMapper trainingRunMapper;
    private final ElasticsearchApiService elasticsearchApiService;
    private final SandboxApiService sandboxApiService;
    private final TrainingFeedbackApiService trainingFeedbackApiService;


    /**
     * Instantiates a new Training instance facade.
     *
     * @param trainingInstanceService   the training instance service
     * @param trainingDefinitionService the training definition service
     * @param trainingRunService        the training run service
     * @param cheatingDetectionService  the cheating detection service
     * @param trainingInstanceMapper    the training instance mapper
     * @param trainingRunMapper         the training run mapper
     * @param userService               the user service
     * @param elasticsearchApiService   the elasticsearch api service
     * @param securityService           the security service
     */
    @Autowired
    public TrainingInstanceFacade(TrainingInstanceService trainingInstanceService,
                                  TrainingDefinitionService trainingDefinitionService,
                                  TrainingRunService trainingRunService,
                                  CheatingDetectionService cheatingDetectionService,
                                  UserService userService,
                                  ElasticsearchApiService elasticsearchApiService,
                                  SecurityService securityService,
                                  SandboxApiService sandboxApiService,
                                  TrainingInstanceMapper trainingInstanceMapper,
                                  TrainingRunMapper trainingRunMapper,
                                  TrainingFeedbackApiService trainingFeedbackApiService) {
        this.trainingInstanceService = trainingInstanceService;
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingRunService = trainingRunService;
        this.cheatingDetectionService = cheatingDetectionService;
        this.userService = userService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.securityService = securityService;
        this.sandboxApiService = sandboxApiService;
        this.trainingInstanceMapper = trainingInstanceMapper;
        this.trainingRunMapper = trainingRunMapper;
        this.trainingFeedbackApiService = trainingFeedbackApiService;
    }

    /**
     * Finds specific Training Instance by id
     *
     * @param id of a Training Instance that would be returned
     * @return specific {@link TrainingInstanceDTO} by id
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#id)")
    @TransactionalRO
    public TrainingInstanceDTO findById(Long id) {
        TrainingInstanceDTO trainingInstanceDTO = trainingInstanceMapper.mapToDTO(trainingInstanceService.findByIdIncludingDefinition(id));
        List<AbstractLevel> levels = trainingDefinitionService.findAllLevelsFromDefinition(trainingInstanceDTO.getTrainingDefinition().getId());
        trainingInstanceDTO.getTrainingDefinition().setHasReferenceSolution(levels.stream()
                .filter(level -> level.getClass() == TrainingLevel.class)
                .anyMatch(trainingLevel -> !((TrainingLevel) trainingLevel).getReferenceSolution().isEmpty()));
        return trainingInstanceDTO;
    }

    /**
     * Find all Training Instances.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingInstanceFindAllResponseDTO}
     */
    @IsOrganizerOrAdmin
    @TransactionalRO
    public PageResultResource<TrainingInstanceFindAllResponseDTO> findAll(Predicate predicate, Pageable pageable) {
        if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
            return trainingInstanceMapper.mapToPageResultResourceBasicView(trainingInstanceService.findAll(predicate, pageable));
        }
        return trainingInstanceMapper.mapToPageResultResourceBasicView(trainingInstanceService.findAll(predicate, pageable, securityService.getUserRefIdFromUserAndGroup()));
    }

    /**
     * Updates training instance
     *
     * @param trainingInstanceUpdateDTO to be updated
     * @return new access token if it was changed
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
                      "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceUpdateDTO.getId())")
    @TransactionalWO
    public String update(TrainingInstanceUpdateDTO trainingInstanceUpdateDTO) {
        TrainingInstance updatedTrainingInstance = trainingInstanceMapper.mapUpdateToEntity(trainingInstanceUpdateDTO);
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceUpdateDTO.getId());

        if (LocalDateTime.now(Clock.systemUTC()).isAfter(trainingInstance.getStartTime()) &&
                !trainingInstance.getTrainingDefinition().getId().equals(trainingInstanceUpdateDTO.getTrainingDefinitionId())) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", Long.class, trainingInstance.getId(),
                    "The training definition assigned to running training instance cannot be changed."));
        }
        checkLocalEnvironmentConfiguration(updatedTrainingInstance);
        updatedTrainingInstance.setTrainingDefinition(trainingDefinitionService.findById(trainingInstanceUpdateDTO.getTrainingDefinitionId()));
        validateVariableNames(updatedTrainingInstance);
        Long oldPoolId = trainingInstance.getPoolId();
        String accessToken = trainingInstanceService.update(updatedTrainingInstance);
        if (isPoolIdChanged(oldPoolId, updatedTrainingInstance.getPoolId())) {
            handlePoolIdModification(oldPoolId, updatedTrainingInstance.getPoolId());
        }
        return accessToken;
    }

    private void validateVariableNames(TrainingInstance trainingInstance) {
        if (trainingInstance.getPoolId() == null && trainingInstance.getSandboxDefinitionId() == null) {
            return;
        }
        Set<String> trainingDefinitionVariables = trainingDefinitionService.getAllTrainingLevels(trainingInstance.getTrainingDefinition().getId()).stream()
                .map(TrainingLevel::getAnswerVariableName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!trainingDefinitionVariables.isEmpty()) {
            Set<String> sandboxDefinitionVariables = trainingInstance.getPoolId() != null ?
                    sandboxApiService.getVariablesByPoolId(trainingInstance.getPoolId()).getVariables() :
                    sandboxApiService.getVariablesBySandboxDefinitionId(trainingInstance.getSandboxDefinitionId());
            if(!sandboxDefinitionVariables.containsAll(trainingDefinitionVariables)) {
                trainingDefinitionVariables.removeAll(sandboxDefinitionVariables);
                throw new EntityConflictException(new EntityErrorDetail("Variable names [" + StringUtils.collectionToCommaDelimitedString(trainingDefinitionVariables) +
                        "] defined in the training definition (ID: " + trainingInstance.getTrainingDefinition().getId() + ") aren't present in the sandbox definition."));
            }
        }

    }

    private void handlePoolIdModification(Long currentPoolId, Long newPoolId) {
        if(currentPoolId == null) {
            sandboxApiService.lockPool(newPoolId);
        } else if (newPoolId == null) {
            sandboxApiService.unlockPool(currentPoolId);
            deleteBashCommandsByPool(currentPoolId);
        } else {
            sandboxApiService.unlockPool(currentPoolId);
            deleteBashCommandsByPool(currentPoolId);
            sandboxApiService.lockPool(newPoolId);
        }
    }

    private boolean isPoolIdChanged(Long currentPoolId, Long updatedPoolId) {
        return (currentPoolId == null && updatedPoolId != null) || (currentPoolId != null && (!currentPoolId.equals(updatedPoolId)));
    }

    /**
     * Creates new training instance
     *
     * @param trainingInstanceCreateDTO to be created
     * @return created {@link TrainingInstanceDTO}
     */
    @IsOrganizerOrAdmin
    @TransactionalWO
    public TrainingInstanceDTO create(TrainingInstanceCreateDTO trainingInstanceCreateDTO) {
        TrainingInstance trainingInstance = trainingInstanceMapper.mapCreateToEntity(trainingInstanceCreateDTO);
        checkLocalEnvironmentConfiguration(trainingInstance);
        trainingInstance.setTrainingDefinition(trainingDefinitionService.findById(trainingInstanceCreateDTO.getTrainingDefinitionId()));
        validateVariableNames(trainingInstance);
        trainingInstance.setId(null);
        TrainingInstance createdTrainingInstance = trainingInstanceService.create(trainingInstance);
        if(trainingInstance.getPoolId() != null) {
            handlePoolIdModification(null, trainingInstance.getPoolId());
        }
        return trainingInstanceMapper.mapToDTO(createdTrainingInstance);
    }

    private void addOrganizersToTrainingInstance(TrainingInstance trainingInstance, Set<Long> userRefIdsOfOrganizers) {
        if (userRefIdsOfOrganizers.isEmpty()) return;
        List<UserRefDTO> organizers = getAllUsersRefsByGivenUsersIds(new ArrayList<>(userRefIdsOfOrganizers));
        Set<Long> actualOrganizersIds = trainingInstance.getOrganizers().stream()
                                                .map(UserRef::getUserRefId)
                                                .collect(Collectors.toSet());
        for (UserRefDTO organizer : organizers) {
            if (actualOrganizersIds.contains(organizer.getUserRefId())) {
                continue;
            }
            try {
                trainingInstance.addOrganizer(userService.getUserByUserRefId(organizer.getUserRefId()));
            } catch (EntityNotFoundException ex) {
                trainingInstance.addOrganizer(userService.createUserRef(createUserRefFromDTO(organizer)));
            }
        }
    }

    private List<UserRefDTO> getAllUsersRefsByGivenUsersIds(List<Long> participantsRefIds) {
        List<UserRefDTO> users = new ArrayList<>();
        PageResultResource<UserRefDTO> usersPageResultResource;
        int page = 0;
        do {
            usersPageResultResource = userService.getUsersRefDTOByGivenUserIds(participantsRefIds, PageRequest.of(page, 999), null, null);
            users.addAll(usersPageResultResource.getContent());
            page++;
        }
        while (page < usersPageResultResource.getPagination().getTotalPages());
        return users;
    }

    private UserRef createUserRefFromDTO(UserRefDTO userToBeCreated) {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userToBeCreated.getUserRefId());
        return userRef;
    }

    /**
     * Deletes specific training instance based on id
     *
     * @param trainingInstanceId of training instance to be deleted
     * @param forceDelete        indicates if this training run should be force deleted.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public void delete(Long trainingInstanceId, boolean forceDelete) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        if (forceDelete) {
            if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() != null) {
                sandboxApiService.unlockPool(trainingInstance.getPoolId());
                deleteBashCommandsByPool(trainingInstance.getPoolId());
            }
        } else if (!trainingInstanceService.checkIfInstanceIsFinished(trainingInstanceId) && trainingRunService.existsAnyForTrainingInstance(trainingInstanceId)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", Long.class, trainingInstanceId,
                    "Active training instance with already assigned training runs cannot be deleted. Please delete training runs assigned to training instance and try again."));
            // not possible to delete active training instances with associated training runs
        } else if (trainingInstance.getPoolId() != null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", Long.class, trainingInstanceId,
                    "First, you must unassign pool id from training instance then try it again."));
            // not possible to delete training instance with associated pool
        }
        Set<TrainingRun> trainingRunsInTrainingInstance = trainingRunService.findAllByTrainingInstanceId(trainingInstanceId);
        trainingRunsInTrainingInstance.forEach(tr -> trainingRunService.deleteTrainingRun(tr.getId(), true, false));
        if (trainingInstance.isLocalEnvironment()) {
            deleteBashCommandsByAccessToken(trainingInstance.getAccessToken());
        }
        trainingInstanceService.delete(trainingInstance);
        cheatingDetectionService.deleteAllCheatingDetectionsOfTrainingInstance(trainingInstanceId);
        trainingFeedbackApiService.deleteAllGraphsByTrainingInstance(trainingInstanceId);
        elasticsearchApiService.deleteEventsByTrainingInstanceId(trainingInstance.getId());
    }

    private void deleteBashCommandsByPool(Long poolId){
        try {
            elasticsearchApiService.deleteCommandsByPool(poolId);
        } catch (MicroserviceApiException ignored){ }
    }

    private void deleteBashCommandsByAccessToken(String accessToken){
        try {
            elasticsearchApiService.deleteCommandsByAccessToken(accessToken);
        } catch (MicroserviceApiException ignored){ }
    }

    /**
     * Assign pool in training instance new training instance
     *
     * @param trainingInstanceId              the training instance id
     * @param trainingInstanceAssignPoolIdDTO of training instance to be deleted
     * @return the training instance basic info dto
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public TrainingInstanceBasicInfoDTO assignPoolToTrainingInstance(Long trainingInstanceId, TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        if (trainingInstance.isLocalEnvironment()) {
            throw new BadRequestException("The pool cannot be assigned to training instance if the local environment is enabled.");
        }
        if (trainingInstance.getPoolId() != null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "Training instance already contains pool Id. Please first unassign pool id and then assign another pool again."));
        }
        // lock pool and update pool
        sandboxApiService.lockPool(trainingInstanceAssignPoolIdDTO.getPoolId());
        trainingInstance.setPoolId(trainingInstanceAssignPoolIdDTO.getPoolId());
        TrainingInstance updatedTrainingInstance = trainingInstanceService.auditAndSave(trainingInstance);
        return trainingInstanceMapper.mapEntityToTIBasicInfo(updatedTrainingInstance);
    }

    /**
     * Reassign pool in training instance  or assignes new training instance
     *
     * @param trainingInstanceId of training instance to be deleted
     * @return the training instance basic info dto
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public TrainingInstanceBasicInfoDTO unassignPoolInTrainingInstance(Long trainingInstanceId) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        if (trainingInstance.getPoolId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "The training instance does not contain any assigned pool already."));
        }
        // unlock previously assigned pool
        sandboxApiService.unlockPool(trainingInstance.getPoolId());
        deleteBashCommandsByPool(trainingInstance.getPoolId());

        trainingInstance.setPoolId(null);
        TrainingInstance updatedTrainingInstance = trainingInstanceService.auditAndSave(trainingInstance);
        return trainingInstanceMapper.mapEntityToTIBasicInfo(updatedTrainingInstance);
    }

    /**
     * Finds all Training Runs by specific Training Instance.
     *
     * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
     * @param isActive           if isActive attribute is True, only active runs are returned
     * @param pageable           pageable parameter with information about pagination.
     * @return Page of {@link TrainingRunDTO} of specific Training Instance
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long trainingInstanceId, Boolean isActive, Pageable pageable) {
        Page<TrainingRun> trainingRuns = trainingInstanceService.findTrainingRunsByTrainingInstance(trainingInstanceId, isActive, pageable);
        Set<Long> runIdsWithWorkingEventLogging = checkLogging(trainingRuns, trainingRunService::checkRunEventLogging);
        Set<Long> runIdsWithWorkingCommandLogging = checkLogging(trainingRuns, trainingRunService::checkRunCommandLogging);
        PageResultResource<TrainingRunDTO> trainingRunDTOsPageResult = trainingRunMapper.mapToPageResultResourceLogging(trainingRuns, runIdsWithWorkingEventLogging, runIdsWithWorkingCommandLogging);
        addParticipantsToTrainingRunDTOs(trainingRunDTOsPageResult.getContent());
        return trainingRunDTOsPageResult;
    }

    private Set<Long> checkLogging(Page<TrainingRun> runs, Function<TrainingRun, Boolean> checker) {
        Set<Long> runIdsWithWorkingLogging = new HashSet<>();
        runs.forEach(trainingRun -> {
            if (checker.apply(trainingRun)) {
                runIdsWithWorkingLogging.add(trainingRun.getId());
            }
        });

        return runIdsWithWorkingLogging;
    }

    private void addParticipantsToTrainingRunDTOs(List<TrainingRunDTO> trainingRunDTOS) {
        trainingRunDTOS.forEach(trainingRunDTO ->
                trainingRunDTO.setParticipantRef(userService.getUserRefDTOByUserRefId(trainingRunDTO.getParticipantRef().getUserRefId())));
    }

    /**
     * Check if instance can be deleted.
     *
     * @param trainingInstanceId the training instance id
     * @return true if instance can be deleted, false if not and message. {@link TrainingInstanceIsFinishedInfoDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public TrainingInstanceIsFinishedInfoDTO checkIfInstanceCanBeDeleted(Long trainingInstanceId) {
        TrainingInstanceIsFinishedInfoDTO infoDTO = new TrainingInstanceIsFinishedInfoDTO();
        if (trainingInstanceService.checkIfInstanceIsFinished(trainingInstanceId)) {
            infoDTO.setHasFinished(true);
            infoDTO.setMessage("Training instance has already finished and can be safely deleted.");
        } else {
            infoDTO.setHasFinished(false);
            infoDTO.setMessage("WARNING: Training instance is still running! Are you sure you want to delete it?");
        }
        return infoDTO;
    }

    /**
     * Retrieve all organizers for given training instance .
     *
     * @param trainingInstanceId id of the training instance for which to get the organizers
     * @param pageable           pageable parameter with information about pagination.
     * @param givenName          optional parameter used for filtration
     * @param familyName         optional parameter used for filtration
     * @return returns all organizers in given training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public PageResultResource<UserRefDTO> getOrganizersOfTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        return userService.getUsersRefDTOByGivenUserIds(trainingInstance.getOrganizers().stream()
                .map(UserRef::getUserRefId)
                .toList(), pageable, givenName, familyName);
    }

    /**
     * Retrieve all organizers not in the given training instance.
     *
     * @param trainingInstanceId id of the training instance which users should be excluded from the result list.
     * @param pageable           pageable parameter with information about pagination.
     * @param givenName          optional parameter used for filtration
     * @param familyName         optional parameter used for filtration
     * @return returns all organizers not in the given training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public PageResultResource<UserRefDTO> getOrganizersNotInGivenTrainingInstance(Long trainingInstanceId, Pageable pageable, String givenName, String familyName) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        Set<Long> excludedOrganizers = trainingInstance.getOrganizers().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet());
        return userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_ORGANIZER, excludedOrganizers, pageable, givenName, familyName);
    }

    /**
     * Concurrently add organizers to the given training instance and remove authors from the training instance.
     *
     * @param trainingInstanceId if of the training instance to be updated
     * @param organizersAddition ids of the organizers to be added to the training instance
     * @param organizersRemoval  ids of the organizers to be removed from the training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalWO
    public void editOrganizers(Long trainingInstanceId, Set<Long> organizersAddition, Set<Long> organizersRemoval) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        Long loggedInUserRefId = securityService.getUserRefIdFromUserAndGroup();
        if (organizersRemoval != null && !organizersRemoval.isEmpty()) {
            organizersRemoval.remove(loggedInUserRefId);
            trainingInstance.removeOrganizersByUserRefIds(organizersRemoval);
        }
        if (organizersAddition != null && !organizersAddition.isEmpty()) {
            addOrganizersToTrainingInstance(trainingInstance, organizersAddition);
        }
        trainingInstanceService.auditAndSave(trainingInstance);
    }

    private void checkLocalEnvironmentConfiguration(TrainingInstance trainingInstance) {
        if (trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() != null) {
            throw new BadRequestException("The pool cannot be assigned to training instance if the local environment is enabled.");
        }

        if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() == null) {
            throw new BadRequestException("The pool must be set if local environment is disabled.");
        }

        if (!trainingInstance.isLocalEnvironment() && trainingInstance.getSandboxDefinitionId() != null) {
            throw new BadRequestException("The sandbox definition cannot be set in the training instance if the local environment is disabled.");
        }
    }
}
