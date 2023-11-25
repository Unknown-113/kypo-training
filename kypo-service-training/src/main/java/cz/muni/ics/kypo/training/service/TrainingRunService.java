package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.QuestionType;
import cz.muni.ics.kypo.training.persistence.model.enums.SubmissionType;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingStatement;
import cz.muni.ics.kypo.training.persistence.model.question.QuestionAnswer;
import cz.muni.ics.kypo.training.persistence.model.question.QuestionChoice;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.service.api.AnswersStorageApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.api.SandboxApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Training run service.
 */
@Service
public class TrainingRunService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunService.class);
    private static final String X_REAL_IP_HEADER = "x-real-ip";

    private final TrainingRunRepository trainingRunRepository;
    private final AbstractLevelRepository abstractLevelRepository;
    private final TrainingInstanceRepository trainingInstanceRepository;
    private final UserRefRepository participantRefRepository;
    private final HintRepository hintRepository;
    private final AuditEventsService auditEventsService;
    private final ElasticsearchApiService elasticsearchApiService;
    private final AnswersStorageApiService answersStorageApiService;
    private final SecurityService securityService;
    private final TRAcquisitionLockRepository trAcquisitionLockRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final SandboxApiService sandboxApiService;
    private final SubmissionRepository submissionRepository;

    /**
     * Instantiates a new Training run service.
     *
     * @param trainingRunRepository       the training run repository
     * @param abstractLevelRepository     the abstract level repository
     * @param trainingInstanceRepository  the training instance repository
     * @param participantRefRepository    the participant ref repository
     * @param hintRepository              the hint repository
     * @param auditEventsService          the audit events service
     * @param securityService             the security service
     * @param sandboxApiService           the python rest template
     * @param trAcquisitionLockRepository the tr acquisition lock repository
     */
    @Autowired
    public TrainingRunService(TrainingRunRepository trainingRunRepository,
                              AbstractLevelRepository abstractLevelRepository,
                              TrainingInstanceRepository trainingInstanceRepository,
                              UserRefRepository participantRefRepository,
                              HintRepository hintRepository,
                              AuditEventsService auditEventsService,
                              ElasticsearchApiService elasticsearchApiService,
                              AnswersStorageApiService answersStorageApiService,
                              SecurityService securityService,
                              QuestionAnswerRepository questionAnswerRepository,
                              SandboxApiService sandboxApiService,
                              TRAcquisitionLockRepository trAcquisitionLockRepository,
                              SubmissionRepository submissionRepository) {
        this.trainingRunRepository = trainingRunRepository;
        this.abstractLevelRepository = abstractLevelRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.participantRefRepository = participantRefRepository;
        this.hintRepository = hintRepository;
        this.auditEventsService = auditEventsService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.answersStorageApiService = answersStorageApiService;
        this.securityService = securityService;
        this.questionAnswerRepository = questionAnswerRepository;
        this.sandboxApiService = sandboxApiService;
        this.trAcquisitionLockRepository = trAcquisitionLockRepository;
        this.submissionRepository = submissionRepository;
    }

    /**
     * Finds specific Training Run by id.
     *
     * @param runId of a Training Run that would be returned
     * @return specific {@link TrainingRun} by id
     * @throws EntityNotFoundException training run is not found.
     */
    public TrainingRun findById(Long runId) {
        return trainingRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId)));
    }

    /**
     * /**
     * Finds specific Training Run by id including current level.
     *
     * @param runId of a Training Run with level that would be returned
     * @return specific {@link TrainingRun} by id
     * @throws EntityNotFoundException training run is not found.
     */
    public TrainingRun findByIdWithLevel(Long runId) {
        return trainingRunRepository.findByIdWithLevel(runId).orElseThrow(() -> new EntityNotFoundException(
                new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId)));
    }

    /**
     * Find all Training Runs.
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return all {@link TrainingRun}s
     */
    public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable) {
        return trainingRunRepository.findAll(predicate, pageable);
    }

    /**
     * Delete selected training run.
     *
     * @param trainingRunId training run to delete
     * @param forceDelete   delete training run in a force manner
     */
    public TrainingRun deleteTrainingRun(Long trainingRunId, boolean forceDelete, boolean deleteDataFromElasticsearch) {
        TrainingRun trainingRun = findById(trainingRunId);
        if (!forceDelete && trainingRun.getState().equals(TRState.RUNNING)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRun.getId().getClass(), trainingRun.getId(),
                    "Cannot delete training run that is running. Consider force delete."));
        }
        questionAnswerRepository.deleteAllByTrainingRunId(trainingRunId);
        submissionRepository.deleteAllByTrainingRunId(trainingRunId);
        if(deleteDataFromElasticsearch) {
            deleteDataFromElasticsearch(trainingRun);
        }
        trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
        trainingRunRepository.delete(trainingRun);
        return trainingRun;
    }

    private void deleteDataFromElasticsearch(TrainingRun trainingRun) {
        if(trainingRun.getTrainingInstance().isLocalEnvironment()) {
            String accessToken = trainingRun.getTrainingInstance().getAccessToken();
            Long userId = trainingRun.getParticipantRef().getUserRefId();
            elasticsearchApiService.deleteCommandsByAccessTokenAndUserId(accessToken, userId);
        } else {
            String sandboxId = trainingRun.getSandboxInstanceRefId() == null ? trainingRun.getPreviousSandboxInstanceRefId() : trainingRun.getSandboxInstanceRefId();
            elasticsearchApiService.deleteCommandsBySandbox(sandboxId);
        }
        elasticsearchApiService.deleteEventsFromTrainingRun(trainingRun.getTrainingInstance().getId(), trainingRun.getId());
    }

    /**
     * Checks whether any training runs exists for particular training instance
     *
     * @param trainingInstanceId the training instance id
     * @return boolean boolean
     */
    public boolean existsAnyForTrainingInstance(Long trainingInstanceId) {
        return trainingRunRepository.existsAnyForTrainingInstance(trainingInstanceId);
    }


    /**
     * Finds all Training Runs of logged in user.
     *
     * @param pageable pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of logged in user.
     */
    public Page<TrainingRun> findAllByParticipantRefUserRefId(Pageable pageable) {
        return trainingRunRepository.findAllByParticipantRefId(securityService.getUserRefIdFromUserAndGroup(), pageable);
    }

    /**
     * Finds all Training Runs of particular training instance.
     *
     * @param trainingInstanceId the training instance id
     * @return the set
     */
    public Set<TrainingRun> findAllByTrainingInstanceId(Long trainingInstanceId) {
        return trainingRunRepository.findAllByTrainingInstanceId(trainingInstanceId);
    }

    /**
     * Move to the next level of given Training Run by setting up a new current level.
     *
     * @param runId id of Training Run whose next level should be returned.
     * @return {@link AbstractLevel}
     * @throws EntityNotFoundException training run or level is not found.
     */
    public TrainingRun moveToNextLevel(Long runId) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        int currentLevelOrder = trainingRun.getCurrentLevel().getOrder();
        int maxLevelOrder = abstractLevelRepository.getCurrentMaxOrder(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        if (!trainingRun.isLevelAnswered()) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId,
                    "You need to answer the level to move to the next level."));
        }
        if (currentLevelOrder == maxLevelOrder) {
            throw new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "There is no next level for current training run (ID: " + runId + ")."));
        }
        List<AbstractLevel> levels = abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        int nextLevelIndex = levels.indexOf(trainingRun.getCurrentLevel()) + 1;
        AbstractLevel abstractLevel = levels.get(nextLevelIndex);
        if (trainingRun.getCurrentLevel() instanceof InfoLevel) {
            auditEventsService.auditLevelCompletedAction(trainingRun);
        }
        trainingRun.setCurrentLevel(abstractLevel);
        trainingRun.setIncorrectAnswerCount(0);
        trainingRunRepository.save(trainingRun);
        auditEventsService.auditLevelStartedAction(trainingRun);

        return trainingRun;
    }

    /**
     * Get previous/current level (visited) of given Training Run.
     *
     * @param runId ID of Training Run whose visited level should be returned.
     * @param levelId ID of the visited level that should be returned.
     * @return {@link AbstractLevel}
     */
    public AbstractLevel getVisitedLevel(Long runId, Long levelId) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        AbstractLevel abstractLevel = abstractLevelRepository.findById(levelId).orElseThrow(
                () -> new EntityNotFoundException(new EntityErrorDetail(AbstractLevel.class, "id", levelId.getClass(), levelId, "Level not found")));
        TrainingDefinition trainingRunDefinition = trainingRun.getTrainingInstance().getTrainingDefinition();
        if (!abstractLevel.getTrainingDefinition().getId().equals(trainingRunDefinition.getId())) {
            throw new EntityConflictException(new EntityErrorDetail("Requested level (ID: " + levelId + ") is not part of the training run (ID: " + runId + ")."));
        }
        if (abstractLevel.getOrder() > trainingRun.getCurrentLevel().getOrder()) {
            throw new EntityConflictException(new EntityErrorDetail("Requested level (ID: " + levelId + ") hasn't been visited yet"));
        }
        return abstractLevel;
    }

    /**
     * Finds all Training Runs of specific Training Definition of logged in user.
     *
     * @param definitionId id of Training Definition
     * @param pageable     pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of specific Training Definition of logged in user
     */
    public Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long definitionId, Pageable pageable) {
        return trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantUserRefId(definitionId, securityService.getUserRefIdFromUserAndGroup(), pageable);
    }

    /**
     * Finds all Training Runs of specific training definition.
     *
     * @param definitionId id of Training Definition whose Training Runs would be returned.
     * @param pageable     pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of specific Training Definition
     */
    public Page<TrainingRun> findAllByTrainingDefinition(Long definitionId, Pageable pageable) {
        return trainingRunRepository.findAllByTrainingDefinitionId(definitionId, pageable);
    }

    /**
     * Gets list of all levels in Training Definition.
     *
     * @param definitionId must be id of first level of some Training Definition.
     * @return List of {@link AbstractLevel}s
     * @throws EntityNotFoundException one of the levels is not found.
     */
    public List<AbstractLevel> getLevels(Long definitionId) {
        return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
    }

    /**
     * Access training run based on given accessToken.
     *
     * @param trainingInstance the training instance
     * @param participantRefId the participant ref id
     * @return accessed {@link TrainingRun}
     * @throws EntityNotFoundException no active training instance for given access token, no starting level in training definition.
     * @throws EntityConflictException pool of sandboxes is not created for training instance.
     */
    public TrainingRun createTrainingRun(TrainingInstance trainingInstance, Long participantRefId) {
        AbstractLevel initialLevel = findFirstLevelForTrainingRun(trainingInstance.getTrainingDefinition().getId());
        TrainingRun trainingRun = getNewTrainingRun(initialLevel, trainingInstance, LocalDateTime.now(Clock.systemUTC()), trainingInstance.getEndTime(), participantRefId);
        return trainingRunRepository.save(trainingRun);
    }

    public void auditTrainingRunStarted(TrainingRun trainingRun) {
        auditEventsService.auditTrainingRunStartedAction(trainingRun);
        auditEventsService.auditLevelStartedAction(trainingRun);
    }

    /**
     * Find running training run of user optional.
     *
     * @param accessToken      the access token
     * @param participantRefId the participant ref id
     * @return the optional
     */
    public Optional<TrainingRun> findRunningTrainingRunOfUser(String accessToken, Long participantRefId) {
        return trainingRunRepository.findRunningTrainingRunOfUser(accessToken, participantRefId);
    }

    /**
     * Gets training instance for particular access token.
     *
     * @param accessToken the access token
     * @return the training instance for particular access token
     */
    public TrainingInstance getTrainingInstanceForParticularAccessToken(String accessToken) {
        TrainingInstance trainingInstance = trainingInstanceRepository.findByStartTimeAfterAndEndTimeBeforeAndAccessToken(LocalDateTime.now(Clock.systemUTC()), accessToken)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken,
                        "There is no active training session matching access token.")));
        if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingInstance.class, "id", trainingInstance.getId().getClass(), trainingInstance.getId(),
                    "At first organizer must allocate sandboxes for training instance."));
        }
        return trainingInstance;
    }

    /**
     * Tr acquisition lock to prevent many requests from the same user. This method is called in a new transaction that means that the existing one is suspended.
     *
     * @param participantRefId   the participant ref id
     * @param trainingInstanceId the training instance id
     * @param accessToken        the access token
     */
    @TransactionalWO(propagation = Propagation.REQUIRES_NEW)
    public void trAcquisitionLockToPreventManyRequestsFromSameUser(Long participantRefId, Long trainingInstanceId, String accessToken) {
        try {
            trAcquisitionLockRepository.saveAndFlush(new TRAcquisitionLock(participantRefId, trainingInstanceId, LocalDateTime.now(Clock.systemUTC())));
        } catch (DataIntegrityViolationException ex) {
            throw new TooManyRequestsException(new EntityErrorDetail(TrainingInstance.class, "accessToken", accessToken.getClass(), accessToken,
                    "Training run has been already accessed and cannot be created again. Please resume Training Run"));
        }
    }

    @TransactionalWO(propagation = Propagation.REQUIRES_NEW)
    public void deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(Long participantRefId, Long trainingInstanceId) {
        trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(participantRefId, trainingInstanceId);
    }

    private AbstractLevel findFirstLevelForTrainingRun(Long trainingDefinitionId) {
        List<AbstractLevel> levels = abstractLevelRepository.findFirstLevelByTrainingDefinitionId(trainingDefinitionId, PageRequest.of(0, 1));
        if (levels.isEmpty()) {
            throw new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class, "id", Long.class, trainingDefinitionId,
                    "No starting level available for this training definition."));
        }
        return levels.get(0);
    }

    private TrainingRun getNewTrainingRun(AbstractLevel currentLevel, TrainingInstance trainingInstance, LocalDateTime startTime, LocalDateTime endTime, Long participantRefId) {
        TrainingRun newTrainingRun = new TrainingRun();
        newTrainingRun.setCurrentLevel(currentLevel);

        Optional<UserRef> userRefOpt = participantRefRepository.findUserByUserRefId(participantRefId);
        if (userRefOpt.isPresent()) {
            newTrainingRun.setParticipantRef(userRefOpt.get());
        } else {
            newTrainingRun.setParticipantRef(participantRefRepository.save(securityService.createUserRefEntityByInfoFromUserAndGroup()));
        }
        newTrainingRun.setAssessmentResponses("[]");
        newTrainingRun.setState(TRState.RUNNING);
        newTrainingRun.setTrainingInstance(trainingInstance);
        newTrainingRun.setStartTime(startTime);
        newTrainingRun.setEndTime(endTime);
        return newTrainingRun;
    }

    /**
     * Connects available sandbox with given Training run.
     *
     * @param trainingRun that will be connected with sandbox
     * @param poolId      the pool id
     * @return Training run with assigned sandbox
     * @throws ForbiddenException       no available sandbox.
     * @throws MicroserviceApiException error calling OpenStack Sandbox Service API
     */
    public TrainingRun assignSandbox(TrainingRun trainingRun, long poolId) {
        String sandboxInstanceRef = sandboxApiService.getAndLockSandbox(poolId).getId();
        trainingRun.setSandboxInstanceRefId(sandboxInstanceRef);
        return trainingRunRepository.save(trainingRun);
    }

    /**
     * Resume previously closed training run.
     *
     * @param trainingRunId id of training run to be resumed.
     * @return {@link TrainingRun}
     * @throws EntityNotFoundException training run is not found.
     */
    public TrainingRun resumeTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        if (trainingRun.getState().equals(TRState.FINISHED) || trainingRun.getState().equals(TRState.ARCHIVED)) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot resume finished training run."));
        }
        if (trainingInstance.getEndTime().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot resume training run after end of training instance."));
        }
        if (!trainingInstance.isLocalEnvironment() && trainingInstance.getPoolId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "The pool assignment of the appropriate training instance has been probably canceled. Please contact the organizer."));
        }

        if (!trainingInstance.isLocalEnvironment() && trainingRun.getSandboxInstanceRefId() == null) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Sandbox of this training run was already deleted, you have to start new training."));
        }
        auditEventsService.auditTrainingRunResumedAction(trainingRun);
        return trainingRun;
    }

    /**
     * Check given answer of given Training Run.
     *
     * @param runId  id of Training Run to check answer.
     * @param answer string which player submit.
     * @return true if answer is correct, false if answer is wrong.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException     the current level of training run is not training level.
     */
    public boolean isCorrectAnswer(Long runId, String answer) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level.getClass() != TrainingLevel.class) {
            throw new BadRequestException("Current level is not training level and does not have answer.");
        } else if (trainingRun.isLevelAnswered()) {
                throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", Long.class, runId, "The answer of the current level of training run has been already corrected."));
        }
        return evaluateTrainingLevelAnswer(trainingRun, answer);
    }

    private boolean evaluateTrainingLevelAnswer(TrainingRun trainingRun, String answer) {
        TrainingLevel trainingLevel = (TrainingLevel) trainingRun.getCurrentLevel();
        String correctAnswer = getTrainingLevelCorrectAnswer(trainingLevel, trainingRun);
        if (correctAnswer.equals(answer)) {
            trainingRun.setLevelAnswered(true);
            trainingRun.increaseTotalTrainingScore(trainingRun.getMaxLevelScore() - trainingRun.getCurrentPenalty());
            auditEventsService.auditCorrectAnswerSubmittedAction(trainingRun, answer);
            auditEventsService.auditLevelCompletedAction(trainingRun);
            auditSubmission(trainingRun, SubmissionType.CORRECT, answer);
            return true;
        } else if (trainingRun.getIncorrectAnswerCount() != trainingLevel.getIncorrectAnswerLimit()) {
            trainingRun.setIncorrectAnswerCount(trainingRun.getIncorrectAnswerCount() + 1);
        }
        auditSubmission(trainingRun, SubmissionType.INCORRECT, answer);
        auditEventsService.auditWrongAnswerSubmittedAction(trainingRun, answer);
        return false;
    }

    /**
     * Check given passkey of given Training Run.
     *
     * @param runId  id of Training Run to check passkey.
     * @param passkey string which player submit.
     * @return true if passkey is correct, false if passkey is wrong.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException     the current level of training run is not access level.
     */
    public boolean isCorrectPassKey(Long runId, String passkey) {
        TrainingRun trainingRun = findByIdWithLevel(runId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level.getClass() != AccessLevel.class) {
            throw new BadRequestException("Current level is not access level and does not have passkey.");
        } else if (trainingRun.isLevelAnswered()) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", Long.class, runId, "The passkey of the current level of training run has been already corrected."));
        }
        return evaluateAccessLevelPasskey(trainingRun, passkey);
    }

    private boolean evaluateAccessLevelPasskey(TrainingRun trainingRun, String passkey) {
        AccessLevel accessLevel = (AccessLevel) trainingRun.getCurrentLevel();
        if (accessLevel.getPasskey().equals(passkey)) {
            trainingRun.setLevelAnswered(true);
            auditEventsService.auditCorrectPasskeySubmittedAction(trainingRun, passkey);
            auditEventsService.auditLevelCompletedAction(trainingRun);
            return true;
        }
        auditEventsService.auditWrongPasskeySubmittedAction(trainingRun, passkey);
        return false;
    }

    private void auditSubmission(TrainingRun trainingRun, SubmissionType submissionType, String answer) {
        Submission submission = new Submission();
        submission.setDate(LocalDateTime.now(Clock.systemUTC()));
        submission.setLevel(trainingRun.getCurrentLevel());
        submission.setTrainingRun(trainingRun);
        submission.setProvided(answer);
        submission.setType(submissionType);
        submission.setIpAddress(getUserIpAddress());
        submissionRepository.save(submission);
    }

    private String getUserIpAddress() {
        ServletRequestAttributes requestAttributes =  ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (requestAttributes != null && requestAttributes.getRequest().getHeader(X_REAL_IP_HEADER) != null) {
            return requestAttributes.getRequest().getHeader(X_REAL_IP_HEADER);
        }
        return "";
    }

    /**
     * Gets remaining attempts to solve current level of training run.
     *
     * @param trainingRunId the training run id
     * @return the remaining attempts
     */
    public int getRemainingAttempts(Long trainingRunId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof TrainingLevel) {
            if (trainingRun.isSolutionTaken()) {
                return 0;
            }
            return ((TrainingLevel) level).getIncorrectAnswerLimit() - trainingRun.getIncorrectAnswerCount();
        }
        throw new BadRequestException("Current level is not training level and does not have answer.");
    }

    /**
     * Gets solution of current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets solution for.
     * @return solution of current level.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException     the current level of training run is not training level.
     */
    public String getSolution(Long trainingRunId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof TrainingLevel trainingLevel) {
            if (!trainingRun.isSolutionTaken()) {
                trainingRun.setSolutionTaken(true);
                trainingRun.addSolutionInfo(new SolutionInfo(trainingLevel.getId(), trainingLevel.getSolution()));
                if (trainingLevel.isSolutionPenalized()) {
                    trainingRun.setCurrentPenalty(trainingRun.getMaxLevelScore());
                }

                trainingRunRepository.save(trainingRun);
                auditEventsService.auditSolutionDisplayedAction(trainingRun);
            }
            return getSolutionWithReplacedVariable(trainingLevel, trainingRun);
        } else {
            throw new BadRequestException("Current level is not training level and does not have solution.");
        }
    }

    private String getSolutionWithReplacedVariable(TrainingLevel trainingLevel, TrainingRun trainingRun) {
        if(!trainingLevel.getSolution().contains("${ANSWER}")) {
            return trainingLevel.getSolution();
        }
        return trainingLevel.getSolution().replaceAll("\\$\\{ANSWER\\}", getTrainingLevelCorrectAnswer(trainingLevel, trainingRun));
    }

    /**
     * Gets correct answer of the training level based on the Training Run parameters.
     *
     * @param trainingLevel Training Level whose correct answer to get.
     * @param trainingRun Training Run of the particular trainee used to obtain variant answer
     * @return static or variant answer based on the Training Run parameters
     */
    public String getTrainingLevelCorrectAnswer(TrainingLevel trainingLevel, TrainingRun trainingRun) {
        if (trainingLevel.isVariantAnswers()) {
            return trainingRun.getTrainingInstance().isLocalEnvironment() ?
                    answersStorageApiService.getCorrectAnswerByLocalSandboxIdAndVariableName(trainingRun.getTrainingInstance().getAccessToken(),
                        trainingRun.getParticipantRef().getUserRefId(), trainingLevel.getAnswerVariableName()) :
                    answersStorageApiService.getCorrectAnswerByCloudSandboxIdAndVariableName(trainingRun.getSandboxInstanceRefId(), trainingLevel.getAnswerVariableName());
        }
        return trainingLevel.getAnswer();
    }

    /**
     * Gets hint of given current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets hint for.
     * @param hintId        id of hint to be returned.
     * @return {@link Hint}
     * @throws EntityNotFoundException training run or hint is not found.
     * @throws BadRequestException     the current level of training run is not training level.
     */
    public Hint getHint(Long trainingRunId, Long hintId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        AbstractLevel level = trainingRun.getCurrentLevel();
        if (level instanceof TrainingLevel) {
            Hint hint = hintRepository.findById(hintId)
                    .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Hint.class, "id", hintId.getClass(), hintId,
                            "Hint not found.")));
            if (hint.getTrainingLevel().getId().equals(level.getId())) {
                trainingRun.increaseCurrentPenalty(hint.getHintPenalty());
                trainingRun.addHintInfo(new HintInfo(level.getId(), hint.getId(), hint.getTitle(), hint.getContent(), hint.getOrder()));
                auditEventsService.auditHintTakenAction(trainingRun, hint);
                return hint;
            }
            throw new EntityConflictException(new EntityErrorDetail(Hint.class, "id", hintId.getClass(), hintId,
                    "Hint is not in current level of training run: " + trainingRunId + "."));
        } else {
            throw new BadRequestException("Current level is not training level and does not have hints.");
        }
    }

    /**
     * Gets max level order of levels from definition.
     *
     * @param definitionId id of training definition.
     * @return max order of levels.
     */
    public int getMaxLevelOrder(Long definitionId) {
        return abstractLevelRepository.getCurrentMaxOrder(definitionId);
    }

    /**
     * Finish training run.
     *
     * @param trainingRunId id of training run to be finished.
     * @throws EntityNotFoundException training run is not found.
     */
    public TrainingRun finishTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        int maxOrder = abstractLevelRepository.getCurrentMaxOrder(trainingRun.getCurrentLevel().getTrainingDefinition().getId());
        if (trainingRun.getCurrentLevel().getOrder() != maxOrder) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot finish training run because current level is not last."));
        }
        if (!trainingRun.isLevelAnswered()) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Cannot finish training run because current level is not answered."));
        }
        trainingRun.setState(TRState.FINISHED);
        trainingRun.setEndTime(LocalDateTime.now(Clock.systemUTC()));
        trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
        if (trainingRun.getCurrentLevel() instanceof InfoLevel) {
            auditEventsService.auditLevelCompletedAction(trainingRun);
        }
        auditEventsService.auditTrainingRunEndedAction(trainingRun);
        return trainingRun;
    }

    /**
     * Archive training run.
     *
     * @param trainingRunId id of training run to be archived.
     * @throws EntityNotFoundException training run is not found.
     */
    public void archiveTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = findById(trainingRunId);
        trainingRun.setState(TRState.ARCHIVED);
        trainingRun.setPreviousSandboxInstanceRefId(trainingRun.getSandboxInstanceRefId());
        trainingRun.setSandboxInstanceRefId(null);
        trAcquisitionLockRepository.deleteByParticipantRefIdAndTrainingInstanceId(trainingRun.getParticipantRef().getUserRefId(), trainingRun.getTrainingInstance().getId());
        trainingRunRepository.save(trainingRun);
    }

    /**
     * Check if run event logging works
     *
     * @param run run to check
     * @return resulting boolean
     */
    public boolean checkRunEventLogging(TrainingRun run) {
        return !elasticsearchApiService.findAllEventsFromTrainingRun(run).isEmpty();
    }

    /**
     * Check if run command logging works
     *
     * @param run run to check
     * @return resulting boolean
     */
    public boolean checkRunCommandLogging(TrainingRun run) {
        List<Map<String, Object>> runCommands;
        if (run.getTrainingInstance().isLocalEnvironment()) {
            String accessToken = run.getTrainingInstance().getAccessToken();
            Long userId = run.getParticipantRef().getUserRefId();
            runCommands = elasticsearchApiService.findAllConsoleCommandsByAccessTokenAndUserId(accessToken, userId);
        } else {
            String sandboxId = run.getSandboxInstanceRefId() == null ? run.getPreviousSandboxInstanceRefId() : run.getSandboxInstanceRefId();
            runCommands = elasticsearchApiService.findAllConsoleCommandsBySandbox(sandboxId);
        }

        return !runCommands.isEmpty();
    }

    /**
     * Evaluate and store responses to assessment.
     *
     * @param trainingRunId      id of training run to be finished.
     * @param answersToQuestions response to assessment to be evaluated
     * @throws EntityNotFoundException training run is not found.
     */
    public void evaluateResponsesToAssessment(Long trainingRunId, Map<Long, QuestionAnswerDTO> answersToQuestions) {
        TrainingRun trainingRun = findByIdWithLevel(trainingRunId);
        if (!(trainingRun.getCurrentLevel() instanceof AssessmentLevel)) {
            throw new BadRequestException("Current level is not assessment level and cannot be evaluated.");
        }
        if (trainingRun.isLevelAnswered())
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(), trainingRunId,
                    "Current level of the training run has been already answered."));
        List<QuestionAnswer> userAnswersToQuestions;
        if (((AssessmentLevel) trainingRun.getCurrentLevel()).getAssessmentType() == AssessmentType.TEST) {
            userAnswersToQuestions = this.gatherAndEvaluateAnswers(trainingRun, answersToQuestions);
        } else {
            userAnswersToQuestions = this.gatherAnswers(trainingRun, answersToQuestions);
        }
        trainingRun.setLevelAnswered(true);
        questionAnswerRepository.saveAll(userAnswersToQuestions);
        auditEventsService.auditAssessmentAnswersAction(trainingRun, userAnswersToQuestions.toString());
        auditEventsService.auditLevelCompletedAction(trainingRun);
    }

    private List<QuestionAnswer> gatherAndEvaluateAnswers(TrainingRun trainingRun, Map<Long, QuestionAnswerDTO> answersToQuestions) {
        int score = 0;
        List<QuestionAnswer> userAnswersToQuestions = new ArrayList<>();
        for (Question question : ((AssessmentLevel) trainingRun.getCurrentLevel()).getQuestions()) {
            QuestionAnswerDTO questionAnswerDTO = answersToQuestions.get(question.getId());
            if (questionAnswerDTO == null) {
                throw new BadRequestException("The question '" + question.getText() + "' must be answered.");
            }
            userAnswersToQuestions.add(this.createQuestionAnswer(question, trainingRun, answersToQuestions.get(question.getId())));
            switch (question.getQuestionType()) {
                case MCQ:
                    score += evaluateMCQ(question, answersToQuestions.get(question.getId()));
                    break;
                case FFQ:
                    score += evaluateFFQ(question, answersToQuestions.get(question.getId()));
                    break;
                case EMI:
                    score += evaluateEMI(question, answersToQuestions.get(question.getId()));
                    break;
                default:
                    break;
            }
        }
        trainingRun.setCurrentPenalty(trainingRun.getMaxLevelScore() - score);
        trainingRun.increaseTotalAssessmentScore(score);
        return userAnswersToQuestions;
    }

    private List<QuestionAnswer> gatherAnswers(TrainingRun trainingRun, Map<Long, QuestionAnswerDTO> answersToQuestions) {
        List<QuestionAnswer> userAnswersToQuestions = new ArrayList<>();
        for (Question question : ((AssessmentLevel) trainingRun.getCurrentLevel()).getQuestions()) {
            QuestionAnswerDTO questionAnswerDTO = answersToQuestions.get(question.getId());
            if (questionAnswerDTO != null) {
                userAnswersToQuestions.add(this.createQuestionAnswer(question, trainingRun, questionAnswerDTO));
            } else if (question.isAnswerRequired()) {
                throw new BadRequestException("The question '" + question.getText() + "' must be answered.");
            }
        }
        return userAnswersToQuestions;
    }

    private QuestionAnswer createQuestionAnswer(Question question, TrainingRun trainingRun, QuestionAnswerDTO answersToQuestion) {
        QuestionAnswer questionAnswer = new QuestionAnswer(question, trainingRun);
        if (question.getQuestionType() == QuestionType.EMI) {
            Set<String> answers = question.getExtendedMatchingStatements().stream()
                    .map(statement -> "{ \"statementOrder\": " + statement.getOrder() + ", \"optionOrder\": " + answersToQuestion.getExtendedMatchingPairs().get(statement.getOrder()) + " }")
                    .collect(Collectors.toSet());
            questionAnswer.setAnswers(answers);
        } else {
            questionAnswer.setAnswers(answersToQuestion.getAnswers());
        }
        return questionAnswer;
    }

    private int evaluateFFQ(Question question, QuestionAnswerDTO userAnswer) {
        List<String> correctAnswers = question.getChoices().stream()
                .map(QuestionChoice::getText)
                .collect(Collectors.toList());
        return correctAnswers.containsAll(userAnswer.getAnswers()) ? question.getPoints() : (-1) * question.getPenalty();
    }

    private int evaluateMCQ(Question question, QuestionAnswerDTO userAnswer) {
        List<String> correctAnswers = question.getChoices().stream()
                .filter(QuestionChoice::isCorrect)
                .map(QuestionChoice::getText)
                .collect(Collectors.toList());
        return userAnswer.getAnswers().size() == correctAnswers.size() &&
                userAnswer.getAnswers().containsAll(correctAnswers) ? question.getPoints() : (-1) * question.getPenalty();
    }

    private int evaluateEMI(Question question, QuestionAnswerDTO userAnswer) {
        for (ExtendedMatchingStatement extendedMatchingStatement : question.getExtendedMatchingStatements()) {
            int expectedOptionOrder = extendedMatchingStatement.getExtendedMatchingOption().getOrder();
            int answeredOptionOrder = userAnswer.getExtendedMatchingPairs().get(extendedMatchingStatement.getOrder());
            if (expectedOptionOrder != answeredOptionOrder) {
                return (-1) * question.getPenalty();
            }
        }
        return question.getPoints();
    }

    public List<QuestionAnswer> getQuestionAnswersByTrainingRunId(Long runId) {
        return questionAnswerRepository.getAllByTrainingRunId(runId);
    }
}
