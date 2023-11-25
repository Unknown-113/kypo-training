package cz.muni.ics.kypo.training.rest.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceAssignPoolIdDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.api.responses.SandboxPoolInfo;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingInstanceMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapperImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.ApiEntityError;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import cz.muni.ics.kypo.training.rest.controllers.TrainingInstancesRestController;
import cz.muni.ics.kypo.training.rest.integration.config.DBTestUtil;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static cz.muni.ics.kypo.commons.security.enums.OIDCItems.*;
import static org.junit.jupiter.api.Assertions.*;
import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertObjectToJsonBytes;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
        TrainingInstancesRestController.class,
        IntegrationTestApplication.class,
        TestDataFactory.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class TrainingInstancesIT {

    private MockMvc mvc;
    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TrainingInstancesRestController trainingInstancesRestController;
    @Autowired
    private TrainingInstanceMapperImpl trainingInstanceMapper;
    @Autowired
    private TrainingRunMapperImpl trainingRunMapper;
    @Autowired
    private TrainingInstanceRepository trainingInstanceRepository;
    @Autowired
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Autowired
    private TrainingRunRepository trainingRunRepository;
    @Autowired
    private UserRefRepository userRefRepository;
    @Autowired
    private InfoLevelRepository infoLevelRepository;
    @Autowired
    @Qualifier("objMapperRESTApi")
    private ObjectMapper mapper;
    @Autowired
    private ElasticsearchApiService elasticsearchApiServiceMock;
    @Autowired
    @Qualifier("userManagementExchangeFunction")
    private ExchangeFunction exchangeFunction;

    private TrainingInstance futureTrainingInstance, notConcludedTrainingInstance, finishedTrainingInstance;
    private TrainingInstanceCreateDTO trainingInstanceCreateDTO;
    private TrainingInstanceUpdateDTO trainingInstanceUpdateDTO;
    private TrainingRun trainingRun1, trainingRun2;
    private UserRef organizer1, organizer2, participant1, participant2;
    private UserRefDTO userRefDTO1, userRefDTO2;
    private SandboxInfo sandboxInfo1, sandboxInfo2;
    private SandboxPoolInfo sandboxPoolInfo;
    private LockedPoolInfo lockedPoolInfo;
    private TrainingInstanceAssignPoolIdDTO trainingInstanceAssignPoolIdDTO;


    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        this.mvc = MockMvcBuilders.standaloneSetup(trainingInstancesRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .setControllerAdvice(new CustomRestExceptionHandlerTraining())
                .build();

        organizer1 = new UserRef();
        organizer1.setUserRefId(1L);
        organizer2 = new UserRef();
        organizer2.setUserRefId(2L);
        participant1 = new UserRef();
        participant1.setUserRefId(3L);
        participant2 = new UserRef();
        participant2.setUserRefId(4L);
        userRefRepository.saveAll(Set.of(organizer1, organizer2, participant1, participant2));

        sandboxInfo1 = new SandboxInfo();
        sandboxInfo1.setId("1L");
        sandboxInfo2 = new SandboxInfo();
        sandboxInfo2.setId("2L");

        sandboxPoolInfo = new SandboxPoolInfo();
        sandboxPoolInfo.setId(15L);
        sandboxPoolInfo.setDefinitionId(10L);
        sandboxPoolInfo.setSize(0L);

        BetaTestingGroup betaTestingGroup = new BetaTestingGroup();
        betaTestingGroup.setOrganizers(new HashSet<>(List.of(organizer1)));

        TrainingDefinition trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setAuthors(new HashSet<>(List.of(organizer1)));
        TrainingDefinition tD = trainingDefinitionRepository.save(trainingDefinition);

        futureTrainingInstance = testDataFactory.getFutureInstance();
        futureTrainingInstance.setTrainingDefinition(tD);
        futureTrainingInstance.setOrganizers(new HashSet<>(List.of(organizer1)));

        finishedTrainingInstance = testDataFactory.getConcludedInstance();
        finishedTrainingInstance.setTrainingDefinition(trainingDefinition);
        finishedTrainingInstance.setOrganizers(new HashSet<>(List.of(organizer1)));

        notConcludedTrainingInstance = testDataFactory.getOngoingInstance();
        notConcludedTrainingInstance.setTrainingDefinition(tD);
        notConcludedTrainingInstance.setOrganizers(new HashSet<>(List.of(organizer1)));

        trainingInstanceCreateDTO = testDataFactory.getTrainingInstanceCreateDTO();
        trainingInstanceCreateDTO.setTrainingDefinitionId(tD.getId());

        trainingInstanceUpdateDTO = testDataFactory.getTrainingInstanceUpdateDTO();
        trainingInstanceUpdateDTO.setTrainingDefinitionId(trainingDefinition.getId());

        InfoLevel iL = testDataFactory.getInfoLevel1();
        InfoLevel infoLevel = infoLevelRepository.save(iL);

        trainingRun1 = testDataFactory.getRunningRun();
        trainingRun1.setCurrentLevel(infoLevel);
        trainingRun1.setTrainingInstance(futureTrainingInstance);
        trainingRun1.setParticipantRef(participant2);
        trainingRun1.setSandboxInstanceRefId(sandboxInfo1.getId());
        trainingRun1.setParticipantRef(organizer1);

        trainingRun2 = testDataFactory.getRunningRun();
        trainingRun2.setSolutionTaken(false);
        trainingRun2.setCurrentLevel(infoLevel);
        trainingRun2.setTrainingInstance(futureTrainingInstance);
        trainingRun2.setParticipantRef(participant1);

        userRefDTO1 = new UserRefDTO();
        userRefDTO1.setUserRefFullName("Ing. John Doe");
        userRefDTO1.setUserRefSub("mail1@muni.cz");
        userRefDTO1.setUserRefGivenName("John");
        userRefDTO1.setUserRefFamilyName("Doe");
        userRefDTO1.setIss("https://oidc.muni.cz");
        userRefDTO1.setUserRefId(3L);

        userRefDTO2 = new UserRefDTO();
        userRefDTO2.setUserRefFullName("Ing. Jan Chudý");
        userRefDTO2.setUserRefSub("mail2@muni.cz");
        userRefDTO2.setUserRefGivenName("Jan");
        userRefDTO2.setUserRefFamilyName("Chudý");
        userRefDTO2.setIss("https://oidc.muni.cz");
        userRefDTO2.setUserRefId(4L);

        trainingRun2.setSandboxInstanceRefId(sandboxInfo2.getId());
        trainingRun2.setParticipantRef(organizer1);

        trainingInstanceAssignPoolIdDTO = new TrainingInstanceAssignPoolIdDTO();
        trainingInstanceAssignPoolIdDTO.setPoolId(1L);

        lockedPoolInfo = new LockedPoolInfo();
        lockedPoolInfo.setId(1L);
        lockedPoolInfo.setPoolId(1L);
    }

    @AfterEach
    public void reset() throws SQLException {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_instance");
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_run");
    }

    @Test
    public void findAllTrainingInstancesAsAdmin() throws Exception {
        trainingInstanceRepository.save(notConcludedTrainingInstance);
        trainingInstanceRepository.save(futureTrainingInstance);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));
        MockHttpServletResponse result = mvc.perform(get("/training-instances"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingInstanceDTO> trainingInstancesPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingInstanceDTO>>() {
        });
        assertTrue(trainingInstancesPage.getContent().contains(trainingInstanceMapper.mapToDTO(notConcludedTrainingInstance)));
        assertTrue(trainingInstancesPage.getContent().contains(trainingInstanceMapper.mapToDTO(futureTrainingInstance)));
    }

    @Test
    public void findTrainingInstanceById() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        sandboxInfo1.setLockId(1);
        MockHttpServletResponse result = mvc.perform(get("/training-instances/{id}", futureTrainingInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        TrainingInstanceDTO expectedInstanceDTO = trainingInstanceMapper.mapToDTO(futureTrainingInstance);
        expectedInstanceDTO.setSandboxesWithTrainingRun(List.of(sandboxInfo1.getId()));
        TrainingInstanceDTO responseInstanceDTO = mapper.readValue(result.getContentAsString(), TrainingInstanceDTO.class);
        assertEquals(expectedInstanceDTO, responseInstanceDTO);
    }

    @Test
    public void findTrainingInstanceByIdNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-instances/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", "100",
                "Entity TrainingInstance (id: 100) not found.");
    }

    @Test
    public void createTrainingInstance() throws Exception {
        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(userRefDTO1));

        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse result = mvc.perform(post("/training-instances").content(convertObjectToJsonBytes(trainingInstanceCreateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Optional<TrainingInstance> newInstance = trainingInstanceRepository.findById(1L);
        assertTrue(newInstance.isPresent());
        TrainingInstanceDTO newInstanceDTO = trainingInstanceMapper.mapToDTO(newInstance.get());

        assertEquals(newInstanceDTO, mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingInstanceDTO.class));
    }

    @Test
    public void createInvalidTrainingInstance() throws Exception {
        Exception ex = mvc.perform(post("/training-instances").content(convertObjectToJsonBytes(new TrainingInstanceCreateDTO()))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();

        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateTrainingInstance() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        trainingInstanceUpdateDTO.setAccessToken(futureTrainingInstance.getAccessToken());
        trainingInstanceUpdateDTO.setId(futureTrainingInstance.getId());
        trainingInstanceUpdateDTO.setPoolId(futureTrainingInstance.getPoolId());
        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(userRefDTO1));

        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse result = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Optional<TrainingInstance> newInstance = trainingInstanceRepository.findById(futureTrainingInstance.getId());
        assertTrue(newInstance.isPresent());

        assertEquals(newInstance.get().getAccessToken(), convertJsonBytesToString(result.getContentAsString()));
    }

    @Test
    public void updateTrainingInstanceTrainingDefinitionNotFound() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        trainingInstanceUpdateDTO.setId(futureTrainingInstance.getId());
        trainingInstanceUpdateDTO.setPoolId(futureTrainingInstance.getPoolId());
        trainingInstanceUpdateDTO.setAccessToken("preff");
        trainingInstanceUpdateDTO.setTrainingDefinitionId(100L);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void updateRunningTrainingInstanceDifferentTrainingDefinition() throws Exception {
        TrainingDefinition trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setTitle("New training definition.");
        trainingDefinitionRepository.save(trainingDefinition);

        trainingInstanceRepository.save(notConcludedTrainingInstance);
        trainingInstanceUpdateDTO.setId(notConcludedTrainingInstance.getId());
        trainingInstanceUpdateDTO.setAccessToken("preff");
        trainingInstanceUpdateDTO.setTrainingDefinitionId(trainingDefinition.getId());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", notConcludedTrainingInstance.getId().toString(),
                "The training definition assigned to running training instance cannot be changed.");
    }

    @Test
    public void updateRunningTrainingInstanceChangeStartTime() throws Exception {
        trainingInstanceRepository.save(notConcludedTrainingInstance);
        trainingInstanceUpdateDTO.setId(notConcludedTrainingInstance.getId());
        trainingInstanceUpdateDTO.setPoolId(notConcludedTrainingInstance.getPoolId());
        trainingInstanceUpdateDTO.setStartTime(trainingInstanceUpdateDTO.getStartTime().plusMinutes(1));
        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(userRefDTO1));

        trainingInstanceUpdateDTO.setAccessToken("preff");
        trainingInstanceUpdateDTO.setTrainingDefinitionId(notConcludedTrainingInstance.getTrainingDefinition().getId());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", notConcludedTrainingInstance.getId().toString(),
                "The start time of the running training instance cannot be changed. Only title can be updated.");
    }

    @Test
    public void updateRunningTrainingInstanceChangeEndTime() throws Exception {
        trainingInstanceRepository.save(notConcludedTrainingInstance);
        trainingInstanceUpdateDTO.setId(notConcludedTrainingInstance.getId());
        trainingInstanceUpdateDTO.setEndTime(trainingInstanceUpdateDTO.getEndTime().plusMinutes(2));
        trainingInstanceUpdateDTO.setStartTime(notConcludedTrainingInstance.getStartTime());
        trainingInstanceUpdateDTO.setPoolId(notConcludedTrainingInstance.getPoolId());

        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(userRefDTO1));

        trainingInstanceUpdateDTO.setTrainingDefinitionId(notConcludedTrainingInstance.getTrainingDefinition().getId());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", notConcludedTrainingInstance.getId().toString(),
                "The end time of the running training instance cannot be changed. Only title can be updated.");
    }

    @Test
    public void updateRunningTrainingInstanceChangeAccessToken() throws Exception {
        trainingInstanceRepository.save(notConcludedTrainingInstance);
        trainingInstanceUpdateDTO.setId(notConcludedTrainingInstance.getId());
        trainingInstanceUpdateDTO.setPoolId(notConcludedTrainingInstance.getPoolId());
        trainingInstanceUpdateDTO.setAccessToken("some bad token");
        trainingInstanceUpdateDTO.setStartTime(notConcludedTrainingInstance.getStartTime());
        trainingInstanceUpdateDTO.setEndTime(notConcludedTrainingInstance.getEndTime());
        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(userRefDTO1));

        trainingInstanceUpdateDTO.setTrainingDefinitionId(notConcludedTrainingInstance.getTrainingDefinition().getId());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", notConcludedTrainingInstance.getId().toString(),
                "The access token of the running training instance cannot be changed. Only title can be updated.");
    }

    @Test
    public void updateTrainingInstanceNotFound() throws Exception {
        trainingInstanceUpdateDTO.setAccessToken("someToken");
        trainingInstanceUpdateDTO.setId(500L);
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", "500",
                "Entity TrainingInstance (id: 500) not found.");
    }

    @Test
    public void updateTrainingInstanceWrongStartTime() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        trainingInstanceUpdateDTO.setAccessToken("someToken");
        trainingInstanceUpdateDTO.setId(futureTrainingInstance.getId());
        trainingInstanceUpdateDTO.setEndTime(LocalDateTime.now().plusHours(2));
        trainingInstanceUpdateDTO.setPoolId(futureTrainingInstance.getPoolId());
        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
        MockHttpServletResponse response = mvc.perform(put("/training-instances")
                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", futureTrainingInstance.getId().toString(),
                "End time must be later than start time.");
    }

    @Test
    public void deleteTrainingInstance() throws Exception {
        futureTrainingInstance.setPoolId(null);
        TrainingInstance tI = trainingInstanceRepository.save(futureTrainingInstance);
        doNothing().when(elasticsearchApiServiceMock).deleteEventsByTrainingInstanceId(anyLong());

        mvc.perform(delete("/training-instances/{id}", tI.getId()))
                .andExpect(status().isOk());
        Optional<TrainingInstance> optTI = trainingInstanceRepository.findById(tI.getId());
        assertFalse(optTI.isPresent());
    }

    @Test
    public void deleteFinishedTrainingInstanceWithUnassignedPool() throws Exception {
        trainingInstanceRepository.save(finishedTrainingInstance);
        trainingRun1.setTrainingInstance(finishedTrainingInstance);
        trainingRunRepository.save(trainingRun1);

        MockHttpServletResponse response = mvc.perform(delete("/training-instances/{id}", finishedTrainingInstance.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", finishedTrainingInstance.getId().toString(),
                "First, you must unassign pool id from training instance then try it again.");
    }

    public void findAllTrainingRunsByTrainingInstanceId() throws Exception {
        trainingInstanceRepository.save(futureTrainingInstance);
        trainingRunRepository.save(trainingRun1);
        trainingRunRepository.save(trainingRun2);
        MockHttpServletResponse result = mvc.perform(get("/training-instances/{instanceId}/training-runs", futureTrainingInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingRunDTO> trainingRunsPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingRunDTO>>() {
        });
        TrainingRunDTO trainingRunDTO1 = trainingRunMapper.mapToDTO(trainingRun1);
        trainingRunDTO1.setParticipantRef(userRefDTO2);
        TrainingRunDTO trainingRunDTO2 = trainingRunMapper.mapToDTO(trainingRun2);
        trainingRunDTO2.setParticipantRef(userRefDTO2);
        assertTrue(trainingRunsPage.getContent().contains(trainingRunDTO1));
        assertTrue(trainingRunsPage.getContent().contains(trainingRunDTO2));
    }

    @Test
    public void findAllTrainingRunsByTrainingInstanceIdWithNonexistentInstance() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-instances/{instanceId}/training-runs", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingInstance.class, "id", "100",
                "Entity TrainingInstance (id: 100) not found.");
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

    private static String convertJsonBytesToString(String object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, String.class);
    }

    private void assertEntityDetailError(EntityErrorDetail entityErrorDetail, Class<?> entity, String identifier, Object value, String reason) {
        assertEquals(entity.getSimpleName(), entityErrorDetail.getEntity());
        assertEquals(identifier, entityErrorDetail.getIdentifier());
        if (entityErrorDetail.getIdentifierValue() == null) {
            assertEquals(value, entityErrorDetail.getIdentifierValue());
        } else {
            assertEquals(value, entityErrorDetail.getIdentifierValue().toString());
        }
        assertEquals(reason, entityErrorDetail.getReason());
    }

    private Mono<ClientResponse> buildMockResponse(Object body) throws IOException{
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .body(convertObjectToJsonBytes(body))
                .header(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return Mono.just(clientResponse);
    }
}

