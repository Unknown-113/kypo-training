package cz.muni.ics.kypo.training.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.ExtendedMatchingStatementDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionAnswerDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.exceptions.errors.JavaApiError;
import cz.muni.ics.kypo.training.exceptions.errors.PythonApiError;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingStatement;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import cz.muni.ics.kypo.training.persistence.model.question.QuestionAnswer;
import cz.muni.ics.kypo.training.persistence.model.question.QuestionChoice;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.service.api.AnswersStorageApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.api.SandboxApiService;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.io.FileReader;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestDataFactory.class})
public class TrainingRunServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TrainingRunService trainingRunService;

    @Autowired
    private TestDataFactory testDataFactory;

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private TRAcquisitionLockRepository trAcquisitionLockRepository;
    @Mock
    private TrainingRunRepository trainingRunRepository;
    @Mock
    private AuditEventsService auditEventService;
    @Mock
    private ElasticsearchApiService elasticsearchApiService;
    @Mock
    private AbstractLevelRepository abstractLevelRepository;
    @Mock
    private TrainingInstanceRepository trainingInstanceRepository;
    @Mock
    private UserRefRepository participantRefRepository;
    @Mock
    private QuestionAnswerRepository questionAnswerRepository;
    @Mock
    private HintRepository hintRepository;
    @Mock
    private ExchangeFunction exchangeFunction;
    @Mock
    private SandboxApiService sandboxApiService;
    @Mock
    private SecurityService securityService;
    @Mock
    private AnswersStorageApiService answersStorageApiService;

    private TrainingRun trainingRun1, trainingRun2;
    private TrainingLevel trainingLevel1, trainingLevel2;
    private AssessmentLevel assessmentLevel;
    private InfoLevel infoLevel, infoLevel2;
    private Hint hint1, hint2;
    private TrainingInstance trainingInstance1, trainingInstance2;
    private UserRef participantRef;
    private SandboxInfo sandboxInfo;
    private TrainingDefinition trainingDefinition, trainingDefinition2;
    private Question ffq, mcq, emi;
    private QuestionAnswerDTO ffqAnswerDTO, mcqAnswerDTO, emiAnswerDTO;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        trainingRunService = new TrainingRunService(trainingRunRepository, abstractLevelRepository, trainingInstanceRepository,
                participantRefRepository, hintRepository, auditEventService, elasticsearchApiService, answersStorageApiService,
                securityService, questionAnswerRepository, sandboxApiService, trAcquisitionLockRepository, submissionRepository);

        trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setId(1L);
        trainingDefinition.setAuthors(new HashSet<>());

        trainingDefinition2 = testDataFactory.getUnreleasedDefinition();
        trainingDefinition2.setId(2L);

        trainingInstance2 = testDataFactory.getConcludedInstance();
        trainingInstance2.setId(2L);
        trainingInstance2.setTrainingDefinition(trainingDefinition2);

        trainingInstance1 = testDataFactory.getOngoingInstance();
        trainingInstance1.setId(1L);
        trainingInstance1.setTrainingDefinition(trainingDefinition);

        participantRef = new UserRef();
        participantRef.setId(1L);
        participantRef.setUserRefId(3L);

        hint1 = testDataFactory.getHint1();
        hint1.setId(1L);

        trainingLevel1 = testDataFactory.getPenalizedLevel();
        trainingLevel1.setId(1L);
        trainingLevel1.setHints(new HashSet<>(Arrays.asList(hint1, hint2)));
        trainingLevel1.setOrder(0);
        trainingLevel1.setTrainingDefinition(trainingDefinition);
        hint1.setTrainingLevel(trainingLevel1);

        trainingLevel2 = testDataFactory.getNonPenalizedLevel();
        trainingLevel2.setId(2L);
        trainingLevel2.setOrder(0);
        trainingLevel2.setTrainingDefinition(trainingDefinition2);

        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel.setId(2L);
        infoLevel.setOrder(1);
        infoLevel.setTrainingDefinition(trainingDefinition);

        infoLevel2 = testDataFactory.getInfoLevel2();
        infoLevel2.setId(2L);
        infoLevel2.setOrder(1);
        infoLevel2.setTrainingDefinition(trainingDefinition2);

        sandboxInfo = new SandboxInfo();
        sandboxInfo.setId(7L);

        trainingRun1 = testDataFactory.getRunningRun();
        trainingRun1.setId(1L);
        trainingRun1.setCurrentLevel(trainingLevel1);
        trainingRun1.setParticipantRef(participantRef);
        trainingRun1.setTrainingInstance(trainingInstance1);
        trainingRun1.setTrainingInstance(trainingInstance1);

        trainingRun2 = testDataFactory.getRunningRun();
        trainingRun2.setId(2L);
        trainingRun2.setCurrentLevel(infoLevel);
        trainingRun2.setParticipantRef(participantRef);
        trainingRun2.setTrainingInstance(trainingInstance2);

        assessmentLevel = testDataFactory.getTest();
        assessmentLevel.setId(3L);

        ffq = testDataFactory.getFreeFormQuestion();
        ffq.setOrder(0);
        ffq.setId(2L);
        ffq.setChoices(testDataFactory.getQuestionChoices(2, "ffq-option", List.of(true, true), ffq));
        mcq = testDataFactory.getMultipleChoiceQuestion();
        mcq.setOrder(1);
        mcq.setId(5L);
        mcq.setChoices(testDataFactory.getQuestionChoices(3, "mcq-option", List.of(true, false, true), mcq));
        emi = testDataFactory.getExtendedMatchingItems();
        emi.setOrder(2);
        emi.setId(1L);
        emi.setExtendedMatchingOptions(testDataFactory.getExtendedMatchingOptions(3, "option", emi));
        emi.setExtendedMatchingStatements(testDataFactory.getExtendedMatchingItems(2, "option", emi, List.of(emi.getExtendedMatchingOptions().get(0), emi.getExtendedMatchingOptions().get(2))));
        assessmentLevel.setQuestions(new ArrayList<>(List.of(ffq, mcq, emi)));

        ffqAnswerDTO = new QuestionAnswerDTO();
        ffqAnswerDTO.setQuestionId(ffq.getId());
        ffqAnswerDTO.setAnswers(ffq.getChoices().stream()
                .map(QuestionChoice::getText)
                .collect(Collectors.toSet()));
        mcqAnswerDTO = new QuestionAnswerDTO();
        mcqAnswerDTO.setQuestionId(mcq.getId());
        mcqAnswerDTO.setAnswers(mcq.getChoices().stream()
                .filter(QuestionChoice::isCorrect)
                .map(QuestionChoice::getText)
                .collect(Collectors.toSet()));
        emiAnswerDTO = new QuestionAnswerDTO();
        emiAnswerDTO.setQuestionId(emi.getId());
        emiAnswerDTO.setExtendedMatchingPairs(emi.getExtendedMatchingStatements().stream()
                .collect(Collectors.toMap(ExtendedMatchingStatement::getOrder, s -> s.getExtendedMatchingOption().getOrder())));
        assessmentLevel.setMaxScore(ffq.getPoints() + mcq.getPoints() + emi.getPoints());
    }

    @Test
    public void getTrainingRunById() {
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));

        TrainingRun t = trainingRunService.findById(trainingRun1.getId());
        assertEquals(t.getId(), trainingRun1.getId());
        assertEquals(t.getState(), trainingRun1.getState());

        then(trainingRunRepository).should().findById(trainingRun1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getTrainingRunByIdNotFound() {
        Long id = 1000L;
        trainingRunService.findById(id);
    }

    @Test
    public void findByIdWithLevel() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));

        Optional<TrainingRun> optionalTrainingRun = trainingRunRepository.findByIdWithLevel(trainingRun1.getId());
        assertTrue(optionalTrainingRun.isPresent());
        assertTrue(optionalTrainingRun.get().getCurrentLevel() instanceof TrainingLevel);
    }

    @Test(expected = EntityNotFoundException.class)
    public void findByIdLevelNotFound() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.empty());
        trainingRunService.findByIdWithLevel(100L);
    }

    @Test
    public void findAll() {
        List<TrainingRun> expected = new ArrayList<>();
        expected.add(trainingRun1);
        expected.add(trainingRun2);

        Page<TrainingRun> page = new PageImpl<>(expected);
        given(trainingRunRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(page);

        Page<TrainingRun> resultPage = trainingRunService.findAll(Expressions.booleanOperation(Ops.IS_NOT_NULL), PageRequest.of(0, 2));
        assertEquals(2, resultPage.getTotalElements());
    }

    @Test
    public void findAllEmpty() {
        Page<TrainingRun> page = new PageImpl<>(new ArrayList<>());
        given(trainingRunRepository.findAll(any(Predicate.class), any(Pageable.class))).willReturn(page);

        Page<TrainingRun> resultPage = trainingRunRepository.findAll(Expressions.booleanOperation(Ops.IS_NOT_NULL), PageRequest.of(0, 2));
        assertEquals(0, resultPage.getTotalElements());
    }

    @Test
    public void deleteFinishedTrainingRun() {
        trainingRun1.setState(TRState.FINISHED);
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        trainingRunService.deleteTrainingRun(trainingRun1.getId(), false);

        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun1.getParticipantRef().getUserRefId(),
                trainingRun1.getTrainingInstance().getId());
        then(trainingRunRepository).should().delete(trainingRun1);
    }

    @Test(expected = EntityConflictException.class)
    public void deleteRunningTrainingRun() {
        trainingRun1.setState(TRState.RUNNING);
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        trainingRunService.deleteTrainingRun(trainingRun1.getId(), false);

        then(trAcquisitionLockRepository).should(never()).deleteByParticipantRefIdAndTrainingInstanceId(trainingRun1.getParticipantRef().getUserRefId(),
                trainingRun1.getTrainingInstance().getId());
        then(trainingRunRepository).should(never()).delete(trainingRun1);
    }

    @Test
    public void deleteRunningTrainingRunForce() {
        trainingRun1.setState(TRState.RUNNING);
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        trainingRunService.deleteTrainingRun(trainingRun1.getId(), true);

        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun1.getParticipantRef().getUserRefId(),
                trainingRun1.getTrainingInstance().getId());
        then(trainingRunRepository).should().delete(trainingRun1);
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteTrainingRunNotFound() {
        given(trainingRunRepository.findById(trainingRun1.getId())).willReturn(Optional.empty());
        trainingRunService.deleteTrainingRun(trainingRun1.getId(), true);

        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun1.getParticipantRef().getUserRefId(),
                trainingRun1.getTrainingInstance().getId());
        then(trainingRunRepository).should().delete(trainingRun1);
    }

    @Test
    public void existsAnyForTrainingInstance() {
        given(trainingRunRepository.existsAnyForTrainingInstance(trainingRun1.getTrainingInstance().getId())).willReturn(true);
        boolean result = trainingRunService.existsAnyForTrainingInstance(trainingRun1.getTrainingInstance().getId());
        assertTrue(result);
    }

    @Test
    public void findAllByParticipantUserRefId() {
        Page<TrainingRun> expectedPage = new PageImpl<>(Arrays.asList(trainingRun1, trainingRun2));

        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participantRef.getUserRefId());
        given(trainingRunRepository.findAllByParticipantRefId(eq(participantRef.getUserRefId()), any(PageRequest.class))).willReturn(expectedPage);
        Page<TrainingRun> resultPage = trainingRunService.findAllByParticipantRefUserRefId(PageRequest.of(0, 2));

        assertEquals(expectedPage, resultPage);
        then(trainingRunRepository).should().findAllByParticipantRefId(participantRef.getUserRefId(), PageRequest.of(0, 2));
    }


    @Test
    public void findAllByTrainingInstanceId() {
        given(trainingRunRepository.findAllByTrainingInstanceId(trainingInstance1.getId())).willReturn(Set.of(trainingRun1, trainingRun2));
        Set<TrainingRun> resultSet = trainingRunService.findAllByTrainingInstanceId(trainingRun1.getTrainingInstance().getId());
        assertEquals(Set.of(trainingRun1, trainingRun2), resultSet);
    }

    @Test
    public void getNextLevel() {
        List<AbstractLevel> levels = new ArrayList<>();
        levels.add(trainingLevel1);
        levels.add(infoLevel);
        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel.getOrder());
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(any(Long.class))).willReturn(levels);
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);

        AbstractLevel resultAbstractLevel = trainingRunService.getNextLevel(trainingRun1.getId());

        assertEquals(trainingRun1.getCurrentLevel().getId(), resultAbstractLevel.getId());
        assertEquals(trainingRun1.getMaxLevelScore(), infoLevel.getMaxScore());
        assertTrue(trainingRun1.isLevelAnswered()); // because next level is info and it is always set to true
        then(trainingRunRepository).should().findByIdWithLevel(trainingRun1.getId());
        then(trainingRunRepository).should().save(trainingRun1);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getNextLevelTrainingRunNotFound() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.empty());
        trainingRunService.getNextLevel(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void getNextLevelNotAnswered() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        trainingRunService.getNextLevel(trainingRun1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getNextLevelNoNextLevel() {
        trainingRun2.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun2));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel2.getOrder());
        trainingRunService.getNextLevel(trainingRun2.getId());
    }

    @Test
    public void findAllByTrainingDefinitionAndParticipant() {
        Page<TrainingRun> expectedPage = new PageImpl<>(Collections.singletonList(trainingRun2));
        given(securityService.getUserRefIdFromUserAndGroup()).willReturn(participantRef.getUserRefId());
        given(trainingRunRepository.findAllByTrainingDefinitionIdAndParticipantUserRefId(any(Long.class), eq(participantRef.getUserRefId()), any(Pageable.class))).willReturn(expectedPage);

        Page<TrainingRun> resultPage = trainingRunService.findAllByTrainingDefinitionAndParticipant(trainingDefinition2.getId(), PageRequest.of(0, 2));

        assertEquals(expectedPage, resultPage);
        then(trainingRunRepository).should().findAllByTrainingDefinitionIdAndParticipantUserRefId(trainingDefinition2.getId(), participantRef.getUserRefId(), PageRequest.of(0, 2));
    }

    @Test
    public void findAllByTrainingDefinition() {
        Page<TrainingRun> expectedPage = new PageImpl<>(Arrays.asList(trainingRun1, trainingRun2));
        given(trainingRunRepository.findAllByTrainingDefinitionId(any(Long.class), any(PageRequest.class))).willReturn(expectedPage);
        Page<TrainingRun> resultPage = trainingRunService.findAllByTrainingDefinition(trainingDefinition.getId(), PageRequest.of(0, 2));
        assertEquals(expectedPage, resultPage);
        then(trainingRunRepository).should().findAllByTrainingDefinitionId(trainingDefinition.getId(), PageRequest.of(0, 2));
    }

    @Test
    public void getLevels() {
        given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingDefinition.getId()))
                .willReturn(List.of(trainingLevel1, trainingLevel2, infoLevel));
        List<AbstractLevel> result = trainingRunService.getLevels(trainingDefinition.getId());
        assertEquals(List.of(trainingLevel1, trainingLevel2, infoLevel), result);
    }

    @Test
    public void createTrainingRun() {
        given(abstractLevelRepository.findFirstLevelByTrainingDefinitionId(eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
                .willReturn(List.of(trainingLevel1));
        given(participantRefRepository.findUserByUserRefId(participantRef.getUserRefId()))
                .willReturn(Optional.of(participantRef));
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);

        TrainingRun trainingRun = trainingRunService.createTrainingRun(trainingInstance1, participantRef.getId());
        then(trainingRunRepository).should().save(any(TrainingRun.class));
        assertEquals(trainingRun1, trainingRun);
    }

    @Test
    public void createTrainingRunNewParticipant() {
        UserRef newParticipant = new UserRef();
        newParticipant.setUserRefId(participantRef.getUserRefId());
        sandboxInfo.setLockId(1);
        given(abstractLevelRepository.findFirstLevelByTrainingDefinitionId(eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
                .willReturn(List.of(trainingLevel1));
        given(participantRefRepository.findUserByUserRefId(participantRef.getUserRefId()))
                .willReturn(Optional.empty());
        given(trainingRunRepository.save(any(TrainingRun.class))).willReturn(trainingRun1);
        given(securityService.createUserRefEntityByInfoFromUserAndGroup()).willReturn(newParticipant);
        given(participantRefRepository.save(newParticipant)).willReturn(participantRef);

        TrainingRun trainingRun = trainingRunService.createTrainingRun(trainingInstance1, participantRef.getId());
        then(trainingRunRepository).should().save(any(TrainingRun.class));
        then(participantRefRepository).should().save(any(UserRef.class));
        assertEquals(trainingRun1, trainingRun);
    }

    @Test(expected = EntityNotFoundException.class)
    public void createTrainingRunNoStartingLevel() {
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        given(abstractLevelRepository.findFirstLevelByTrainingDefinitionId(eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
                .willReturn(List.of());
        trainingRunService.createTrainingRun(trainingInstance1, participantRef.getId());
    }

    @Test(expected = MicroserviceApiException.class)
    public void createTrainingRunUserManagementError() {
        trainingInstance1.setTrainingDefinition(trainingDefinition);
        given(abstractLevelRepository.findFirstLevelByTrainingDefinitionId(eq(trainingInstance1.getTrainingDefinition().getId()), any(Pageable.class)))
                .willReturn(List.of(trainingLevel1));
        given(participantRefRepository.findUserByUserRefId(participantRef.getUserRefId()))
                .willReturn(Optional.empty());
        willThrow(new MicroserviceApiException(HttpStatus.CONFLICT, JavaApiError.of("Error when calling user managements service"))).given(securityService).createUserRefEntityByInfoFromUserAndGroup();
        trainingRunService.createTrainingRun(trainingInstance1, participantRef.getId());
        then(trainingRunRepository).should(never()).save(any(TrainingRun.class));
    }


    @Test
    public void findRunningTrainingRunOfUser() {
        given(trainingRunRepository.findRunningTrainingRunOfUser(trainingInstance1.getAccessToken(), participantRef.getUserRefId()))
                .willReturn(Optional.of(trainingRun1));
        Optional<TrainingRun> result = trainingRunService.findRunningTrainingRunOfUser(trainingInstance1.getAccessToken(), participantRef.getUserRefId());
        assertTrue(result.isPresent());
    }

    @Test
    public void trAcquisitionLockToPreventManyRequestsFromSameUser() {
        TRAcquisitionLock trAcquisitionLock = new TRAcquisitionLock(participantRef.getUserRefId(), trainingInstance1.getId(), LocalDateTime.now());
        trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(participantRef.getUserRefId(), trainingInstance1.getId(), trainingInstance1.getAccessToken());
        then(trAcquisitionLockRepository).should().saveAndFlush(trAcquisitionLock);
    }

    @Test(expected = TooManyRequestsException.class)
    public void trAcquisitionLockToPreventManyRequestsFromSameUser_AlreadyExists() {
        TRAcquisitionLock trAcquisitionLock = new TRAcquisitionLock(participantRef.getUserRefId(), trainingInstance1.getId(), LocalDateTime.now());
        willThrow(DataIntegrityViolationException.class).given(trAcquisitionLockRepository).saveAndFlush(trAcquisitionLock);
        trainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(participantRef.getUserRefId(), trainingInstance1.getId(), trainingInstance1.getAccessToken());
    }

    @Test
    public void deleteTrAcquisitionLockToPreventManyRequestsFromSameUser() {
        trainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(participantRef.getId(), trainingInstance1.getId());
        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(participantRef.getId(), trainingInstance1.getId());
    }

    @Test
    public void assignSandbox() {
        trainingRun1.setSandboxInstanceRefId(null);
        given(sandboxApiService.getAndLockSandbox(anyLong())).willReturn(sandboxInfo);
        trainingRunService.assignSandbox(trainingRun1, trainingRun1.getTrainingInstance().getPoolId());
        then(trainingRunRepository).should().save(trainingRun1);
        then(auditEventService).should().auditTrainingRunStartedAction(trainingRun1);
        then(auditEventService).should().auditLevelStartedAction(trainingRun1);
        assertEquals(sandboxInfo.getId(), trainingRun1.getSandboxInstanceRefId());
    }

    @Test(expected = ForbiddenException.class)
    public void assignSandboxNoAvailable() {
        trainingRun1.setSandboxInstanceRefId(null);
        willThrow(new ForbiddenException("There is no available sandbox, wait a minute and try again or ask organizer to allocate more sandboxes.")).given(sandboxApiService).getAndLockSandbox(anyLong());
        trainingRunService.assignSandbox(trainingRun1, trainingRun1.getTrainingInstance().getPoolId());
        then(trainingRunRepository).should(never()).save(trainingRun1);
    }

    @Test(expected = MicroserviceApiException.class)
    public void assignSandboxMicroserviceException() {
        trainingRun1.setSandboxInstanceRefId(null);
        willThrow(new MicroserviceApiException("Error", new CustomWebClientException(HttpStatus.NOT_FOUND, PythonApiError.of("Some error")))).given(sandboxApiService).getAndLockSandbox(anyLong());
        trainingRunService.assignSandbox(trainingRun1, trainingRun1.getTrainingInstance().getPoolId());
        then(trainingRunRepository).should(never()).save(trainingRun1);
    }

    @Test
    public void resumeTrainingRun() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        TrainingRun trainingRun = trainingRunService.resumeTrainingRun(trainingRun1.getId());

        assertEquals(trainingRun.getId(), trainingRun1.getId());
        assertTrue(trainingRun.getCurrentLevel() instanceof TrainingLevel);
    }

    @Test(expected = EntityNotFoundException.class)
    public void resumeTrainingRunNotFound() {
        trainingRun1.setSandboxInstanceRefId(null);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.empty());
        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void resumeTrainingRunFinished() {
        trainingRun1.setState(TRState.FINISHED);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void resumeTrainingRunTrainingInstanceFinished() {
        trainingRun1.getTrainingInstance().setStartTime(LocalDateTime.now(Clock.systemUTC()).minusHours(2));
        trainingRun1.getTrainingInstance().setEndTime(LocalDateTime.now(Clock.systemUTC()).minusHours(1));
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void resumeTrainingRunDeletedSandbox() {
        trainingRun1.setSandboxInstanceRefId(null);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.resumeTrainingRun(trainingRun1.getId());
    }

    @Test
    public void isCorrectAnswer() {
        int scoreBefore = trainingRun1.getTotalTrainingScore() + trainingRun1.getTotalAssessmentScore();
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        boolean isCorrect = trainingRunService.isCorrectAnswer(trainingRun1.getId(), trainingLevel1.getAnswer());
        assertTrue(isCorrect);
        assertEquals(scoreBefore + (trainingRun1.getMaxLevelScore() - trainingRun1.getCurrentPenalty()), trainingRun1.getTotalTrainingScore() + trainingRun1.getTotalAssessmentScore());
        assertTrue(trainingRun1.isLevelAnswered());
        then(auditEventService).should().auditCorrectAnswerSubmittedAction(trainingRun1, trainingLevel1.getAnswer());
        then(auditEventService).should().auditLevelCompletedAction(trainingRun1);
        then(submissionRepository).should().save(any(Submission.class));
    }

    @Test
    public void isCorrectAnswerVariableName() {
        String variableCorrectAnswer = "correct-answer";
        trainingLevel1.setAnswerVariableName("variable-secret");
        trainingDefinition.setVariantSandboxes(true);
        int scoreBefore = trainingRun1.getTotalTrainingScore() + trainingRun1.getTotalAssessmentScore();
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        given(answersStorageApiService.getCorrectAnswerBySandboxIdAndVariableName(trainingRun1.getSandboxInstanceRefId(), trainingLevel1.getAnswerVariableName())).willReturn(variableCorrectAnswer);
        boolean isCorrect = trainingRunService.isCorrectAnswer(trainingRun1.getId(), variableCorrectAnswer);
        assertTrue(isCorrect);
        assertEquals(scoreBefore + (trainingRun1.getMaxLevelScore() - trainingRun1.getCurrentPenalty()), trainingRun1.getTotalTrainingScore() + trainingRun1.getTotalAssessmentScore());
        assertTrue(trainingRun1.isLevelAnswered());
        then(answersStorageApiService).should().getCorrectAnswerBySandboxIdAndVariableName(trainingRun1.getSandboxInstanceRefId(), trainingLevel1.getAnswerVariableName());
        then(auditEventService).should().auditCorrectAnswerSubmittedAction(trainingRun1, variableCorrectAnswer);
        then(auditEventService).should().auditLevelCompletedAction(trainingRun1);
        then(submissionRepository).should().save(any(Submission.class));
    }

    @Test
    public void isNotCorrectAnswer() {
        String wrongAnswer = "wrong answer";
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        boolean isCorrect = trainingRunService.isCorrectAnswer(trainingRun1.getId(), wrongAnswer);
        assertFalse(isCorrect);
        assertFalse(trainingRun1.isLevelAnswered());
        then(auditEventService).should().auditWrongAnswerSubmittedAction(trainingRun1, wrongAnswer);
        then(submissionRepository).should().save(any(Submission.class));
    }

    @Test
    public void isVariableNameNotCorrect() {
        String variableCorrectAnswer = "correct-answer";
        trainingLevel1.setAnswerVariableName("variable-secret");
        trainingDefinition.setVariantSandboxes(true);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        given(answersStorageApiService.getCorrectAnswerBySandboxIdAndVariableName(trainingRun1.getSandboxInstanceRefId(), trainingLevel1.getAnswerVariableName())).willReturn(variableCorrectAnswer);
        boolean isCorrect = trainingRunService.isCorrectAnswer(trainingRun1.getId(), variableCorrectAnswer + "aaa");
        assertFalse(isCorrect);
        assertFalse(trainingRun1.isLevelAnswered());
        then(answersStorageApiService).should().getCorrectAnswerBySandboxIdAndVariableName(trainingRun1.getSandboxInstanceRefId(), trainingLevel1.getAnswerVariableName());
        then(auditEventService).should().auditWrongAnswerSubmittedAction(trainingRun1, variableCorrectAnswer + "aaa");
        then(submissionRepository).should().save(any(Submission.class));
        then(submissionRepository).should().save(any(Submission.class));
    }

    @Test(expected = BadRequestException.class)
    public void isCorrectAnswerOfNonTrainingLevel() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
        trainingRunService.isCorrectAnswer(trainingRun2.getId(), "answer");
    }

    @Test
    public void getRemainingAttempts() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.ofNullable(trainingRun1));
        int attempts = trainingRunService.getRemainingAttempts(trainingRun1.getId());
        assertEquals(trainingLevel1.getIncorrectAnswerLimit() - trainingRun1.getIncorrectAnswerCount(), attempts);
    }

    @Test
    public void getRemainingAttemptsWhenSolutionHasBeenTaken() {
        trainingRun1.setSolutionTaken(true);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.ofNullable(trainingRun1));
        int attempts = trainingRunService.getRemainingAttempts(trainingRun1.getId());
        assertEquals(0, attempts);
    }

    @Test(expected = BadRequestException.class)
    public void getRemainingAttemptsOfNonTrainingLevel() {
        trainingRun1.setCurrentLevel(assessmentLevel);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.ofNullable(trainingRun1));
        trainingRunService.getRemainingAttempts(trainingRun1.getId());
    }

    @Test
    public void getSolution() {
        trainingRun1.setTotalTrainingScore(40);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        String solution = trainingRunService.getSolution(trainingRun1.getId());
        assertEquals(solution, trainingLevel1.getSolution());
        assertFalse(trainingRun1.isLevelAnswered());
        then(auditEventService).should().auditSolutionDisplayedAction(trainingRun1);
    }

    @Test
    public void getSolutionAlreadyTaken() {
        trainingRun1.setSolutionTaken(true);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        String solution = trainingRunService.getSolution(trainingRun1.getId());
        assertEquals(solution, trainingLevel1.getSolution());
        assertFalse(trainingRun1.isLevelAnswered());
        then(auditEventService).should(never()).auditSolutionDisplayedAction(trainingRun1);
    }

    @Test(expected = BadRequestException.class)
    public void getSolutionOfNonTrainingLevel() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
        trainingRunService.getSolution(trainingRun2.getId());
    }

    @Test
    public void getHint() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(hintRepository.findById(any(Long.class))).willReturn(Optional.of(hint1));
        Hint resultHint1 = trainingRunService.getHint(trainingRun1.getId(), hint1.getId());
        assertEquals(hint1, resultHint1);
        assertEquals(hint1.getHintPenalty(), (Integer) trainingRun1.getCurrentPenalty());
        then(auditEventService).should().auditHintTakenAction(trainingRun1, resultHint1);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getHintNotFound() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        given(hintRepository.findById(Long.MAX_VALUE)).willReturn(Optional.empty());
        trainingRunService.getHint(trainingRun1.getId(), Long.MAX_VALUE);
    }

    @Test(expected = EntityConflictException.class)
    public void getHintNotInTrainingLevel() {
        hint1.setTrainingLevel(trainingLevel2);
        given(trainingRunRepository.findByIdWithLevel(trainingRun1.getId())).willReturn(Optional.of(trainingRun1));
        given(hintRepository.findById(hint1.getId())).willReturn(Optional.of(hint1));
        trainingRunService.getHint(trainingRun1.getId(), hint1.getId());
    }

    @Test(expected = BadRequestException.class)
    public void getHintNonTrainingLevel() {
        given(trainingRunRepository.findByIdWithLevel(trainingRun2.getId())).willReturn(Optional.of(trainingRun2));
        trainingRunService.getHint(trainingRun2.getId(), hint1.getId());
    }

    @Test
    public void testFinishTrainingRunWithTrainingLevel() {
        trainingRun2.setCurrentLevel(trainingLevel1);
        trainingRun2.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun2));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(trainingLevel1.getOrder());
        trainingRunService.finishTrainingRun(trainingRun2.getId());
        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun2.getParticipantRef().getUserRefId(), trainingRun2.getTrainingInstance().getId());
        then(auditEventService).should().auditTrainingRunEndedAction(trainingRun2);
        assertEquals(trainingRun2.getState(), TRState.FINISHED);
    }

    @Test
    public void testFinishTrainingRunWithInfoLevel() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun2));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel2.getOrder());
        trainingRunService.finishTrainingRun(trainingRun2.getId());
        then(trAcquisitionLockRepository).should().deleteByParticipantRefIdAndTrainingInstanceId(trainingRun2.getParticipantRef().getUserRefId(), trainingRun2.getTrainingInstance().getId());
        then(auditEventService).should().auditLevelCompletedAction(trainingRun2);
        then(auditEventService).should().auditTrainingRunEndedAction(trainingRun2);
        assertEquals(trainingRun2.getState(), TRState.FINISHED);
    }


    @Test(expected = EntityConflictException.class)
    public void testFinishTrainingRunNonLastLevel() {
        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        given(abstractLevelRepository.getCurrentMaxOrder(anyLong())).willReturn(infoLevel.getOrder());
        trainingRunService.finishTrainingRun(trainingRun1.getId());
    }

    @Test(expected = EntityConflictException.class)
    public void testFinishTrainingRunNotAnsweredLevel() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.finishTrainingRun(trainingRun1.getId());
    }

    @Test
    public void evaluateResponsesToAssessmentAllCorrect() {
        int scoreBefore = trainingRun1.getTotalAssessmentScore();
        trainingRun1.setCurrentPenalty(0);
        trainingRun1.setCurrentLevel(assessmentLevel);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.evaluateResponsesToAssessment(trainingRun1.getId(), Map.of(ffqAnswerDTO.getQuestionId(), ffqAnswerDTO,
                mcqAnswerDTO.getQuestionId(), mcqAnswerDTO, emiAnswerDTO.getQuestionId(), emiAnswerDTO));
        assertEquals(scoreBefore + ffq.getPoints() + mcq.getPoints() + emi.getPoints(), trainingRun1.getTotalAssessmentScore());
        assertEquals(0, trainingRun1.getCurrentPenalty());
        assertTrue(trainingRun1.isLevelAnswered());
        then(questionAnswerRepository).should().saveAll(any());
        then(auditEventService).should().auditAssessmentAnswersAction(any(TrainingRun.class), any(String.class));
        then(auditEventService).should().auditLevelCompletedAction(trainingRun1);
    }

    @Test
    public void evaluateResponsesToAssessmentFFQIncorrect() {
        ffqAnswerDTO.setAnswers(Set.of("something incorrect"));
        trainingRun1.setCurrentPenalty(0);
        int scoreBefore = trainingRun1.getTotalAssessmentScore();
        trainingRun1.setCurrentLevel(assessmentLevel);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.evaluateResponsesToAssessment(trainingRun1.getId(), Map.of(ffqAnswerDTO.getQuestionId(), ffqAnswerDTO,
                mcqAnswerDTO.getQuestionId(), mcqAnswerDTO, emiAnswerDTO.getQuestionId(), emiAnswerDTO));
        assertEquals(scoreBefore + mcq.getPoints() + emi.getPoints() - ffq.getPenalty(), trainingRun1.getTotalAssessmentScore());
        assertEquals(ffq.getPoints() + ffq.getPenalty(), trainingRun1.getCurrentPenalty());
    }

    @Test
    public void evaluateResponsesToAssessmentMCQIncorrect() {
        mcq.setPenalty(4);
        mcqAnswerDTO.setAnswers(Set.of(mcq.getChoices().get(0).getText()));
        int scoreBefore = trainingRun1.getTotalAssessmentScore();
        trainingRun1.setCurrentLevel(assessmentLevel);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.evaluateResponsesToAssessment(trainingRun1.getId(), Map.of(ffqAnswerDTO.getQuestionId(), ffqAnswerDTO,
                mcqAnswerDTO.getQuestionId(), mcqAnswerDTO, emiAnswerDTO.getQuestionId(), emiAnswerDTO));
        assertEquals(scoreBefore - mcq.getPenalty() + emi.getPoints() + ffq.getPoints(), trainingRun1.getTotalAssessmentScore());
        assertEquals(mcq.getPoints() + mcq.getPenalty(), trainingRun1.getCurrentPenalty());

    }

    @Test
    public void evaluateResponsesToAssessmentEmiIncorrect() {
        emi.setPenalty(3);
        emiAnswerDTO.setExtendedMatchingPairs(Map.of(0, 0, 1, 1));
        int scoreBefore = trainingRun1.getTotalAssessmentScore();
        trainingRun1.setCurrentLevel(assessmentLevel);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.evaluateResponsesToAssessment(trainingRun1.getId(), Map.of(ffqAnswerDTO.getQuestionId(), ffqAnswerDTO,
                mcqAnswerDTO.getQuestionId(), mcqAnswerDTO, emiAnswerDTO.getQuestionId(), emiAnswerDTO));
        assertEquals(scoreBefore + mcq.getPoints() - emi.getPenalty() + ffq.getPoints(), trainingRun1.getTotalAssessmentScore());
        assertEquals(emi.getPoints() + emi.getPenalty(), trainingRun1.getCurrentPenalty());

    }

    @Test(expected = BadRequestException.class)
    public void evaluateResponsesToAssessmentNonAssessmentLevel() {
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.evaluateResponsesToAssessment(trainingRun1.getId(), Map.of());
    }

    @Test(expected = EntityConflictException.class)
    public void evaluateResponsesToAssessmentAlreadyAnswered() {
        trainingRun1.setCurrentLevel(assessmentLevel);
        trainingRun1.setLevelAnswered(true);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.evaluateResponsesToAssessment(trainingRun1.getId(), Map.of());
    }

    @Test(expected = BadRequestException.class)
    public void evaluateResponsesToAssessmentQuestionNotAnswered() {
        trainingRun1.setCurrentLevel(assessmentLevel);
        given(trainingRunRepository.findByIdWithLevel(any(Long.class))).willReturn(Optional.of(trainingRun1));
        trainingRunService.evaluateResponsesToAssessment(trainingRun1.getId(), Map.of(Long.MAX_VALUE, ffqAnswerDTO,
                mcqAnswerDTO.getQuestionId(), mcqAnswerDTO, emiAnswerDTO.getQuestionId(), emiAnswerDTO));
    }

    @After
    public void after() {
        reset(trainingRunRepository);
    }
}
