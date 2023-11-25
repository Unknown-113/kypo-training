package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsTrainee;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.accesslevel.AccessLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.LevelReferenceSolutionDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingOption;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingStatement;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.service.SecurityService;
import cz.muni.ics.kypo.training.service.api.TrainingFeedbackApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type Training definition facade.
 */
@Service
@Transactional
public class TrainingDefinitionFacade {

    private final TrainingDefinitionService trainingDefinitionService;
    private final TrainingFeedbackApiService trainingFeedbackApiService;
    private final UserService userService;
    private final SecurityService securityService;
    private final TrainingDefinitionMapper trainingDefinitionMapper;
    private final LevelMapper levelMapper;

    /**
     * Instantiates a new Training definition facade.
     *
     * @param trainingDefinitionService the training definition service
     * @param trainingDefMapper         the training def mapper
     * @param levelMapper               the level mapper
     * @param userService               the user service
     * @param securityService           the security service
     */
    @Autowired
    public TrainingDefinitionFacade(TrainingDefinitionService trainingDefinitionService,
                                    TrainingFeedbackApiService trainingFeedbackApiService,
                                    UserService userService,
                                    SecurityService securityService,
                                    TrainingDefinitionMapper trainingDefMapper,
                                    LevelMapper levelMapper) {
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingFeedbackApiService = trainingFeedbackApiService;
        this.userService = userService;
        this.securityService = securityService;
        this.trainingDefinitionMapper = trainingDefMapper;
        this.levelMapper = levelMapper;
    }

    /**
     * Finds specific Training Definition by id
     *
     * @param id of a Training Definition that would be returned
     * @return specific {@link TrainingDefinitionByIdDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
    @TransactionalRO
    public TrainingDefinitionByIdDTO findById(Long id) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(id);
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionMapper.mapToDTOById(trainingDefinition);
        trainingDefinitionByIdDTO.setLevels(gatherLevels(id));
        if (trainingDefinition.getBetaTestingGroup() != null) {
            trainingDefinitionByIdDTO.setBetaTestingGroupId(trainingDefinition.getBetaTestingGroup().getId());
        }
        return trainingDefinitionByIdDTO;
    }

    private List<BasicLevelInfoDTO> gatherBasicLevelInfo(Long definitionId) {
        List<AbstractLevel> levels = trainingDefinitionService.findAllLevelsFromDefinition(definitionId);
        List<BasicLevelInfoDTO> levelInfoDTOs = new ArrayList<>();

        levels.forEach(level -> {
            BasicLevelInfoDTO basicLevelInfoDTO = new BasicLevelInfoDTO();
            basicLevelInfoDTO.setId(level.getId());
            basicLevelInfoDTO.setTitle(level.getTitle());
            basicLevelInfoDTO.setOrder(level.getOrder());
            if (level instanceof TrainingLevel)
                basicLevelInfoDTO.setLevelType(LevelType.TRAINING_LEVEL);
            else if (level instanceof AssessmentLevel)
                basicLevelInfoDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
            else if (level instanceof AccessLevel)
                basicLevelInfoDTO.setLevelType(LevelType.ACCESS_LEVEL);
            else
                basicLevelInfoDTO.setLevelType(LevelType.INFO_LEVEL);
            levelInfoDTOs.add(basicLevelInfoDTO);
        });
        return levelInfoDTOs;
    }

    private List<AbstractLevelDTO> gatherLevels(Long definitionId) {
        List<AbstractLevel> levels = trainingDefinitionService.findAllLevelsFromDefinition(definitionId);
        return levels.stream()
                .map(this.levelMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find all Training Definitions.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingDefinitionDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalRO
    public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable) {
        if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
            return mapToDtoAndAddArchivingInfo(trainingDefinitionService.findAll(predicate, pageable));
        } else {
            Long loggedInUserId = securityService.getUserRefIdFromUserAndGroup();
            return mapToDtoAndAddArchivingInfo(trainingDefinitionService.findAll(predicate, pageable, loggedInUserId));
        }
    }

    private PageResultResource<TrainingDefinitionDTO> mapToDtoAndAddArchivingInfo(Page<TrainingDefinition> trainingDefinitionPage) {
        PageResultResource<TrainingDefinitionDTO> resource = trainingDefinitionMapper.mapToPageResultResource(trainingDefinitionPage);
        for (TrainingDefinitionDTO trainingDefinitionDTO : resource.getContent()) {
            trainingDefinitionDTO.setCanBeArchived(checkIfCanBeArchived(trainingDefinitionDTO.getId()));
        }
        return resource;
    }

    /**
     * Find all Training Definitions.
     *
     * @param state    represents a string if the training definitions should be released or not.
     * @param pageable pageable parameter with information about pagination.
     * @return page of all {@link TrainingDefinitionInfoDTO} accessible for organizers
     */
    @IsOrganizerOrAdmin
    @TransactionalRO
    public PageResultResource<TrainingDefinitionInfoDTO> findAllForOrganizers(TDState state, Pageable pageable) {
        Long loggedInUserId = securityService.getUserRefIdFromUserAndGroup();
        if (state == TDState.RELEASED) {
            return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(
                    trainingDefinitionService.findAllByState(cz.muni.ics.kypo.training.persistence.model.enums.TDState.RELEASED, pageable));
        } else if (state == TDState.UNRELEASED) {
            if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
                return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(
                        trainingDefinitionService.findAllByState(cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED, pageable));
            } else if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_DESIGNER) && securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)) {
                return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(
                        trainingDefinitionService.findAllForDesignersAndOrganizersUnreleased(loggedInUserId, pageable));
            } else {
                return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(
                        trainingDefinitionService.findAllForOrganizersUnreleased(loggedInUserId, pageable));
            }
        }
        throw new InternalServerErrorException("It is required to provide training definition state that is RELEASED or UNRELEASED");
    }

    /**
     * Creates new training definition
     *
     * @param trainingDefinition to be created
     * @return DTO of created definition, {@link TrainingDefinitionCreateDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalWO
    public TrainingDefinitionByIdDTO create(TrainingDefinitionCreateDTO trainingDefinition) {
        TrainingDefinition newTrainingDefinition = trainingDefinitionMapper.mapCreateToEntity(trainingDefinition);
        if (trainingDefinition.getBetaTestingGroup() != null) {
            addOrganizersToTrainingDefinition(newTrainingDefinition, trainingDefinition.getBetaTestingGroup().getOrganizersRefIds());
        }
        TrainingDefinition createdTrainingDefinition = trainingDefinitionService.create(newTrainingDefinition, trainingDefinition.isDefaultContent());
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionMapper.mapToDTOById(createdTrainingDefinition);
        if (createdTrainingDefinition.getBetaTestingGroup() != null) {
            trainingDefinitionByIdDTO.setBetaTestingGroupId(createdTrainingDefinition.getBetaTestingGroup().getId());
        }
        return trainingDefinitionByIdDTO;
    }

    /**
     * Updates training definition
     *
     * @param trainingDefinitionUpdateDTO to be updated
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#trainingDefinitionUpdateDTO.getId())")
    @TransactionalWO
    public void update(TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
        TrainingDefinition mappedTrainingDefinition = trainingDefinitionMapper.mapUpdateToEntity(trainingDefinitionUpdateDTO);
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionUpdateDTO.getId());
        mappedTrainingDefinition.setAuthors(new HashSet<>(trainingDefinition.getAuthors()));
        if (trainingDefinitionUpdateDTO.getBetaTestingGroup() != null) {
            addOrganizersToTrainingDefinition(mappedTrainingDefinition, trainingDefinitionUpdateDTO.getBetaTestingGroup().getOrganizersRefIds());
            if (trainingDefinition.getBetaTestingGroup() != null) {
                trainingDefinition.getBetaTestingGroup().setId(trainingDefinition.getBetaTestingGroup().getId());
            }
        } else if (trainingDefinition.getBetaTestingGroup() != null) {
            throw new EntityConflictException(new EntityErrorDetail(BetaTestingGroup.class, "id", Long.class, trainingDefinition.getBetaTestingGroup().getId(),
                    "Cannot delete beta testing group. You only can remove organizers from group."));
        }
        trainingDefinitionService.update(mappedTrainingDefinition);
    }

    private void addOrganizersToTrainingDefinition(TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
        trainingDefinition.getBetaTestingGroup().setOrganizers(new HashSet<>());
        PageResultResource<UserRefDTO> organizers = userService.getUsersRefDTOByGivenUserIds(new ArrayList<>(userRefIds), PageRequest.of(0, 999), null, null);
        for (UserRefDTO organizer : organizers.getContent()) {
            try {
                trainingDefinition.getBetaTestingGroup().addOrganizer(userService.getUserByUserRefId(organizer.getUserRefId()));
            } catch (EntityNotFoundException ex) {
                trainingDefinition.getBetaTestingGroup().addOrganizer(userService.createUserRef(createUserRefFromDTO(organizer)));
            }
        }
    }

    private UserRef createUserRefFromDTO(UserRefDTO userToBeCreated) {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userToBeCreated.getUserRefId());
        return userRef;
    }

    /**
     * Clones Training Definition by id
     *
     * @param id    of definition to be cloned
     * @param title the title of cloned definition
     * @return DTO of cloned definition, {@link TrainingDefinitionByIdDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
    @TransactionalWO
    public TrainingDefinitionByIdDTO clone(Long id, String title) {
        TrainingDefinitionByIdDTO clonedDefinition = trainingDefinitionMapper.mapToDTOById(trainingDefinitionService.clone(id, title));
        clonedDefinition.setLevels(gatherLevels(clonedDefinition.getId()));
        this.updateReferenceSolution(clonedDefinition.getId());
        return clonedDefinition;
    }

    /**
     * Swaps between levels. Swap basically means swapping the order attribute between these two levels.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param swapLevelFrom - Id of a first level to be swapped.
     * @param swapLevelTo   - Id of a second level to be swapped.
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<BasicLevelInfoDTO> swapLevels(Long definitionId, Long swapLevelFrom, Long swapLevelTo) {
        trainingDefinitionService.swapLevels(definitionId, swapLevelFrom, swapLevelTo);
        this.updateReferenceSolution(definitionId);
        return gatherBasicLevelInfo(definitionId);
    }

    /**
     * Move level to the different position and modify orders of levels between moved level and new position.
     *
     * @param definitionId     - Id of definition containing levels, this training definition is updating its last edited column.
     * @param levelIdToBeMoved - id of the level to be moved to the new position
     * @param newPosition      - position where level will be moved
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<BasicLevelInfoDTO> moveLevel(Long definitionId, Long levelIdToBeMoved, Integer newPosition) {
        trainingDefinitionService.moveLevel(definitionId, levelIdToBeMoved, newPosition);
        this.updateReferenceSolution(definitionId);
        return gatherBasicLevelInfo(definitionId);
    }

    /**
     * Deletes specific training instance based on id
     *
     * @param id of definition to be deleted
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
    @TransactionalWO
    public void delete(Long id) {
        trainingDefinitionService.delete(id);
        trainingFeedbackApiService.deleteReferenceGraph(id);
    }

    /**
     * deletes specific level by id
     *
     * @param definitionId - id of definition containing level to be deleted
     * @param levelId      - id of level to be deleted
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<BasicLevelInfoDTO> deleteOneLevel(Long definitionId, Long levelId) {
        trainingDefinitionService.deleteOneLevel(definitionId, levelId);
        this.updateReferenceSolution(definitionId);
        return gatherBasicLevelInfo(definitionId);
    }

    /**
     * updates info level from training definition
     *
     * @param definitionId - id of training definition containing levels to be updated
     * @param updatedLevelDTOs  updated levels to be stored
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateLevels(Long definitionId, List<AbstractLevelUpdateDTO> updatedLevelDTOs) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(definitionId);
        trainingDefinitionService.checkIfCanBeUpdated(trainingDefinition);
        Map<Long, AbstractLevel> persistedLevelsById = trainingDefinitionService.findAllLevelsFromDefinition(definitionId).stream()
                .collect(Collectors.toMap(AbstractLevel::getId, Function.identity()));
        boolean referenceSolutionChanged = false;
        for(var updatedLevelDTO : updatedLevelDTOs) {
            AbstractLevel persistedLevel = persistedLevelsById.get(updatedLevelDTO.getId());
            if(persistedLevel == null) {
                throw new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", Long.class,
                        updatedLevelDTO.getId(), "Level was not found in definition (id: " + definitionId + ")."));
            }
            switch (updatedLevelDTO.getLevelType()) {
                case TRAINING_LEVEL:
                    TrainingLevel updatedTrainingLevel = levelMapper.mapUpdateToEntity((TrainingLevelUpdateDTO) updatedLevelDTO);
                    referenceSolutionChanged = referenceSolutionChanged || !updatedTrainingLevel.getReferenceSolution()
                            .equals(((TrainingLevel)persistedLevel).getReferenceSolution());
                    trainingDefinitionService.updateTrainingLevel(updatedTrainingLevel, (TrainingLevel) persistedLevel);
                    break;
                case ACCESS_LEVEL:
                    AccessLevel updatedAccessLevel = levelMapper.mapUpdateToEntity((AccessLevelUpdateDTO) updatedLevelDTO);
                    trainingDefinitionService.updateAccessLevel(updatedAccessLevel, (AccessLevel) persistedLevel);
                    break;
                case INFO_LEVEL:
                    InfoLevel updatedInfoLevel = levelMapper.mapUpdateToEntity((InfoLevelUpdateDTO) updatedLevelDTO);
                    trainingDefinitionService.updateInfoLevel(updatedInfoLevel, (InfoLevel) persistedLevel);
                    break;
                case ASSESSMENT_LEVEL:
                    AssessmentLevel updatedAssessmentLevel = levelMapper.mapUpdateToEntity((AssessmentLevelUpdateDTO) updatedLevelDTO);
                    if (updatedAssessmentLevel.getAssessmentType() == AssessmentType.TEST) {
                        this.checkAndSetCorrectOptionsOfStatements(updatedAssessmentLevel, (AssessmentLevelUpdateDTO) updatedLevelDTO);
                    }
                    trainingDefinitionService.updateAssessmentLevel(updatedAssessmentLevel, (AssessmentLevel) persistedLevel);
                    break;
            }
        }
        if (referenceSolutionChanged) {
            updateReferenceSolution(definitionId);
        }

        this.trainingDefinitionService.auditAndSave(trainingDefinition);
    }

    private void updateReferenceSolution(Long definitionId) {
        boolean isAnyReferenceSolution = false;
        List<LevelReferenceSolutionDTO> referenceSolution = new ArrayList<>();
        for (TrainingLevel level : this.trainingDefinitionService.getAllTrainingLevels(definitionId)) {
            referenceSolution.add(new LevelReferenceSolutionDTO(level.getId(), level.getOrder(), new ArrayList<>(ReferenceSolutionNodeMapper.INSTANCE.mapToSetDTO(level.getReferenceSolution()))));
            isAnyReferenceSolution = isAnyReferenceSolution || !level.getReferenceSolution().isEmpty();
        }
        this.trainingFeedbackApiService.deleteReferenceGraph(definitionId);
        if(isAnyReferenceSolution) {
            this.trainingFeedbackApiService.createReferenceGraph(definitionId, referenceSolution);
        }
    }

    /**
     * updates training level from training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param trainingLevel    to be updated
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateTrainingLevel(Long definitionId, TrainingLevelUpdateDTO trainingLevel) {
        TrainingLevel trainingLevelToUpdate = levelMapper.mapUpdateToEntity(trainingLevel);
        TrainingLevel updatedTrainingLevel = trainingDefinitionService.updateTrainingLevel(definitionId, trainingLevelToUpdate);
        this.updateReferenceSolution(definitionId);
        this.trainingDefinitionService.auditAndSave(updatedTrainingLevel.getTrainingDefinition());
    }

    /**
     * updates info level from training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param infoLevel    to be updated
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) {
        InfoLevel updatedInfoLevel = trainingDefinitionService.updateInfoLevel(definitionId, levelMapper.mapUpdateToEntity(infoLevel));
        this.trainingDefinitionService.auditAndSave(updatedInfoLevel.getTrainingDefinition());
    }

    /**
     * updates assessment level from training definition
     *
     * @param definitionId    - id of training definition containing level to be updated
     * @param assessmentLevelToUpdate to be updated
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateAssessmentLevel(Long definitionId, AssessmentLevelUpdateDTO assessmentLevelToUpdate) {
        AssessmentLevel assessmentLevel = levelMapper.mapUpdateToEntity(assessmentLevelToUpdate);
        if (assessmentLevel.getAssessmentType() == AssessmentType.TEST) {
            this.checkAndSetCorrectOptionsOfStatements(assessmentLevel, assessmentLevelToUpdate);
        }
        AssessmentLevel updatedAssessmentLevel = trainingDefinitionService.updateAssessmentLevel(definitionId, assessmentLevel);
        this.trainingDefinitionService.auditAndSave(updatedAssessmentLevel.getTrainingDefinition());
    }

    private void checkAndSetCorrectOptionsOfStatements(AssessmentLevel assessmentLevel, AssessmentLevelUpdateDTO assessmentLevelUpdateDTO) {
        assessmentLevelUpdateDTO.getQuestions().stream()
                .filter(questionDTO -> questionDTO.getQuestionType() == cz.muni.ics.kypo.training.api.enums.QuestionType.EMI)
                .forEach(questionDTO -> questionDTO.getExtendedMatchingStatements()
                        .forEach(statementDTO -> {
                            if (statementDTO.getCorrectOptionOrder() == null) {
                                throw new BadRequestException("You must set the correct option for the each statement in the assessment of the type TEST");
                            }
                            Question question = assessmentLevel.getQuestions().get(questionDTO.getOrder());
                            ExtendedMatchingOption correctOption = question.getExtendedMatchingOptions().get(statementDTO.getCorrectOptionOrder());
                            ExtendedMatchingStatement statementToUpdate = question.getExtendedMatchingStatements().get(statementDTO.getOrder());
                            statementToUpdate.setExtendedMatchingOption(correctOption);
                        }));
    }

    /**
     * creates new info level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new info level
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createInfoLevel(Long definitionId) {
        InfoLevel newInfoLevel = trainingDefinitionService.createInfoLevel(definitionId);
        return levelMapper.mapTo(newInfoLevel);
    }

    /**
     * creates new training level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new training level
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createTrainingLevel(Long definitionId) {
        TrainingLevel newTrainingLevel = trainingDefinitionService.createTrainingLevel(definitionId);
        return levelMapper.mapTo(newTrainingLevel);
    }

    /**
     * Creates new access level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new access level
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createAccessLevel(Long definitionId) {
        AccessLevel newAccessLevel = trainingDefinitionService.createAccessLevel(definitionId);
        return levelMapper.mapTo(newAccessLevel);
    }

    /**
     * creates new assessment level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new assessment level
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createAssessmentLevel(Long definitionId) {
        AssessmentLevel newAssessmentLevel = trainingDefinitionService.createAssessmentLevel(definitionId);
        return levelMapper.mapTo(newAssessmentLevel);
    }

    /**
     * Finds specific level by id
     *
     * @param levelId - id of wanted level
     * @return wanted {@link AbstractLevelDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalRO
    public AbstractLevelDTO findLevelById(Long levelId) {
        return levelMapper.mapToDTO(trainingDefinitionService.findLevelById(levelId));
    }

    /**
     * Get users with given role
     *
     * @param roleType   the wanted role type
     * @param pageable   pageable parameter with information about pagination.
     * @param givenName  the given name
     * @param familyName the family name
     * @return list of users {@link UserRefDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalRO
    public PageResultResource<UserRefDTO> getUsersWithGivenRole(RoleType roleType, Pageable pageable, String givenName, String familyName) {
        return userService.getUsersByGivenRole(roleType, pageable, givenName, familyName);
    }

    /**
     * Switch state of definition to unreleased
     *
     * @param definitionId - id of training definition
     * @param state        - new state of TD
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void switchState(Long definitionId, TDState state) {
        trainingDefinitionService.switchState(definitionId, state);
    }

    private boolean checkIfCanBeArchived(Long definitionId) {
        List<TrainingInstance> instances = trainingDefinitionService.findAllTrainingInstancesByTrainingDefinitionId(definitionId);
        for (TrainingInstance trainingInstance : instances) {
            if (trainingInstance.getEndTime().isAfter(LocalDateTime.now(Clock.systemUTC()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieve all authors for given training definition.
     *
     * @param trainingDefinitionId id of the training definition for which to get the authors
     * @param pageable             pageable parameter with information about pagination.
     * @param givenName            optional parameter used for filtration
     * @param familyName           optional parameter used for filtration
     * @return returns all authors in given training definition.
     */
    @IsDesignerOrOrganizerOrAdmin
    public PageResultResource<UserRefDTO> getAuthors(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        return userService.getUsersRefDTOByGivenUserIds(trainingDefinition.getAuthors().stream()
                        .map(UserRef::getUserRefId)
                        .toList(),
                pageable, givenName, familyName);
    }

    /**
     * Retrieve all beta testers for given training definition.
     *
     * @param trainingDefinitionId id of the training definition for which to get the beta testers
     * @param pageable             pageable parameter with information about pagination.
     * @return returns all beta testers in given training definition.
     */
    @IsDesignerOrOrganizerOrAdmin
    public PageResultResource<UserRefDTO> getBetaTesters(Long trainingDefinitionId, Pageable pageable) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        if (trainingDefinition.getBetaTestingGroup() != null && !trainingDefinition.getBetaTestingGroup().getOrganizers().isEmpty()) {
            return userService.getUsersRefDTOByGivenUserIds(trainingDefinition.getBetaTestingGroup().getOrganizers().stream()
                    .map(UserRef::getUserRefId)
                    .toList(), pageable, null, null);
        }
        return new PageResultResource<>(Collections.emptyList(), new PageResultResource.Pagination(0, 0, 0, 0, 0));
    }

    /**
     * Retrieve all designers not in the given training definition.
     *
     * @param trainingDefinitionId id of the training definition which users should be excluded from the result list.
     * @param pageable             pageable parameter with information about pagination.
     * @param givenName            optional parameter used for filtration
     * @param familyName           optional parameter used for filtration
     * @return returns all designers not in the given training definition.
     */
    @IsDesignerOrOrganizerOrAdmin
    @TransactionalRO
    public PageResultResource<UserRefDTO> getDesignersNotInGivenTrainingDefinition(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        Set<Long> excludedUsers = trainingDefinition.getAuthors().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet());
        return userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_DESIGNER, excludedUsers, pageable, givenName, familyName);
    }

    /**
     * Concurrently add authors to the given training definition and remove authors from the training definition.
     *
     * @param trainingDefinitionId if of the training definition to be updated
     * @param authorsAddition      ids of the authors to be added to the training definition
     * @param authorsRemoval       ids of the authors to be removed from the training definition.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#trainingDefinitionId)")
    @TransactionalWO
    public void editAuthors(Long trainingDefinitionId, Set<Long> authorsAddition, Set<Long> authorsRemoval) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        Long loggedInUserRefId = securityService.getUserRefIdFromUserAndGroup();
        if (authorsRemoval != null && !authorsRemoval.isEmpty()) {
            authorsRemoval.remove(loggedInUserRefId);
            trainingDefinition.removeAuthorsByUserRefIds(authorsRemoval);
        }
        if (authorsAddition != null && !authorsAddition.isEmpty()) {
            addAuthorsToTrainingDefinition(trainingDefinition, authorsAddition);
        }
        trainingDefinitionService.auditAndSave(trainingDefinition);
    }

    /**
     * Check if the reference solution is defined for the given training definition.
     *
     * @param trainingDefinitionId the training definition id
     * @return true if at least one of the training levels has reference solution defined, false otherwise.
     */
    @IsTrainee
    @TransactionalRO
    public boolean hasReferenceSolution(Long trainingDefinitionId) {
        return trainingDefinitionService.hasReferenceSolution(trainingDefinitionId);
    }

    //TODO: If one of the to-be-added users is a Trainee, fail with exception, atomically preferably
    private void addAuthorsToTrainingDefinition(TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
        List<UserRefDTO> authors = getAllUsersRefsByGivenUsersIds(new ArrayList<>(userRefIds));
        Set<Long> actualAuthorsIds = trainingDefinition.getAuthors().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet());
        for (UserRefDTO author : authors) {
            if (actualAuthorsIds.contains(author.getUserRefId())) {
                continue;
            }
            try {
                trainingDefinition.addAuthor(userService.getUserByUserRefId(author.getUserRefId()));
            } catch (EntityNotFoundException ex) {
                trainingDefinition.addAuthor(userService.createUserRef(createUserRefFromDTO(author)));
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
}
