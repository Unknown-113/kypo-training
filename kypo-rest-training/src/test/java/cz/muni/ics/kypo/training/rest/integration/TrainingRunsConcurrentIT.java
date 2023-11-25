package cz.muni.ics.kypo.training.rest.integration;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.ValidateAnswerDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import cz.muni.ics.kypo.training.rest.controllers.TrainingInstancesRestController;
import cz.muni.ics.kypo.training.rest.controllers.TrainingRunsRestController;
import cz.muni.ics.kypo.training.service.AuditEventsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cz.muni.ics.kypo.commons.security.enums.OIDCItems.ISS;
import static cz.muni.ics.kypo.commons.security.enums.OIDCItems.SUB;
import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest(classes = {
        TrainingInstancesRestController.class,
        TrainingRunsRestController.class,
        IntegrationTestApplication.class,
        TestDataFactory.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
@TestPropertySource(properties = {"openstack-server.uri=http://localhost:8080"})
@RunWith(ConcurrentTestRunner.class)
public class TrainingRunsConcurrentIT {

    private MockMvc mvc;

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private TrainingRunsRestController trainingRunsRestController;
    @Autowired
    private TrainingInstancesRestController trainingInstancesRestController;
    @Autowired
    private TrainingRunRepository trainingRunRepository;
    @Autowired
    private TrainingInstanceRepository trainingInstanceRepository;
    @Autowired
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Autowired
    private UserRefRepository userRefRepository;
    @Autowired
    private TrainingLevelRepository trainingLevelRepository;
    @Autowired
    private AssessmentLevelRepository assessmentLevelRepository;
    @Autowired
    private TRAcquisitionLockRepository trAcquisitionLockRepository;
    @Autowired
    @Qualifier("sandboxManagementExchangeFunction")
    private ExchangeFunction sandboxManagementExchangeFunction;
    @Autowired
    @Qualifier("userManagementExchangeFunction")
    private ExchangeFunction userManagementExchangeFunction;

    @MockBean
    private AuditEventsService auditEventsService;


    private TrainingLevel trainingLevel;
    private AssessmentLevel assessmentLevel;
    private UserRefDTO userRefDTO1;
    private TrainingInstance trainingInstance;
    private TrainingDefinition trainingDefinition;
    private UserRef participant1, participant2;
    private SandboxInfo sandboxInfo1;
    private TrainingRun trainingRunTrainingLevel, trainingRunAssessmentLevel;

    @Before
    public void init() throws Exception {
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        MockitoAnnotations.openMocks(this);
        this.mvc = MockMvcBuilders.standaloneSetup(trainingRunsRestController, trainingInstancesRestController)
                .setControllerAdvice(new CustomRestExceptionHandlerTraining())
                .build();
        sandboxInfo1 = new SandboxInfo();
        sandboxInfo1.setId("1L");
        sandboxInfo1.setLockId(1);

        participant1 = new UserRef();
        participant1.setUserRefId(3L);
        participant2 = new UserRef();
        participant2.setUserRefId(4L);
        userRefRepository.saveAll(Set.of(participant1, participant2));

        userRefDTO1 = new UserRefDTO();
        userRefDTO1.setUserRefFullName("Ing. John Doe");
        userRefDTO1.setUserRefSub("mail@muni.cz");
        userRefDTO1.setUserRefGivenName("John");
        userRefDTO1.setUserRefFamilyName("Doe");
        userRefDTO1.setIss("https://oidc.muni.cz");
        userRefDTO1.setUserRefId(3L);

        trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinitionRepository.save(trainingDefinition);

        trainingInstance = testDataFactory.getOngoingInstance();
        trainingInstance.setTrainingDefinition(trainingDefinition);
        trainingInstance = trainingInstanceRepository.save(trainingInstance);

        trainingLevel = testDataFactory.getPenalizedLevel();
        trainingLevel.setTrainingDefinition(trainingDefinition);
        trainingLevel.setOrder(1);
        trainingLevel.setIncorrectAnswerLimit(3);
        trainingLevelRepository.save(trainingLevel);

        assessmentLevel = testDataFactory.getQuestionnaire();
        assessmentLevel.setTrainingDefinition(trainingDefinition);
        assessmentLevel.setOrder(2);
        assessmentLevelRepository.save(assessmentLevel);

        trainingRunAssessmentLevel = this.testDataFactory.getRunningRun();
        trainingRunAssessmentLevel.setCurrentLevel(assessmentLevel);
        trainingRunAssessmentLevel.setTrainingInstance(trainingInstance);
        trainingRunAssessmentLevel.setParticipantRef(participant2);

        trainingRunTrainingLevel = this.testDataFactory.getRunningRun();
        trainingRunTrainingLevel.setCurrentLevel(trainingLevel);
        trainingRunTrainingLevel.setIncorrectAnswerCount(0);
        trainingRunTrainingLevel.setTrainingInstance(trainingInstance);
        trainingRunTrainingLevel.setParticipantRef(participant2);
        trainingRunRepository.saveAll(Set.of(trainingRunAssessmentLevel, trainingRunTrainingLevel));

    }

    @Test
    @ThreadCount(4)
    public void concurrentAccessTrainingRun() throws Exception {
        given(userManagementExchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(userRefDTO1));
        given(sandboxManagementExchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(sandboxInfo1));
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        mvc.perform(post("/training-runs")
                .param("accessToken", trainingInstance.getAccessToken()));
    }

    @Test
    @ThreadCount(4)
    public void concurrentAssessmentEvaluation() throws Exception {
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        mvc.perform(put("/training-runs/" + trainingRunAssessmentLevel.getId() + "/assessment-evaluations")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(new ArrayList<>())));
        then(auditEventsService).should(times(1)).auditAssessmentAnswersAction(any(), any());
    }

    @Test
    @ThreadCount(4)
    public void concurrentIsCorrectAnswer() throws Exception {
        ValidateAnswerDTO validateAnswerDTO = new ValidateAnswerDTO();
        validateAnswerDTO.setAnswer(trainingLevel.getAnswer());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_TRAINEE.name()));
        mvc.perform(post("/training-runs/" + trainingRunTrainingLevel.getId() + "/is-correct-answer")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(validateAnswerDTO)));
        then(auditEventsService).should(times(1)).auditCorrectAnswerSubmittedAction(any(), any());
    }

    @After
    public void testAccessTrainingRun() throws Exception {
        List<TrainingRun> trainingRuns = trainingRunRepository.findAll();
        List<TRAcquisitionLock> locks = trAcquisitionLockRepository.findAll();
        assertEquals(3, trainingRuns.size());
        assertEquals(1, locks.size());
    }

    private Mono<ClientResponse> buildMockResponse(Object body) throws IOException {
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .body(convertObjectToJsonBytes(body))
                .header(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return Mono.just(clientResponse);
    }

    private void mockSpringSecurityContextForGet(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        Jwt jwt = new Jwt("bearer-token-value", null, null, Map.of("alg", "HS256"),
                Map.of(ISS.getName(), "oidc-issuer", SUB.getName(), "mail@muni.cz"));
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, authorities);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
