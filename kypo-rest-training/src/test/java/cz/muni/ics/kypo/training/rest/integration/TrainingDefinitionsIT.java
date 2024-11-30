package cz.muni.ics.kypo.training.rest.integration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.dto.technique.MitreTechniqueDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.QuestionType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.mapping.mapstruct.LevelMapperImpl;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingDefinitionMapperImpl;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingOption;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import cz.muni.ics.kypo.training.persistence.repository.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.rest.ApiEntityError;
import cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining;
import cz.muni.ics.kypo.training.rest.controllers.TrainingDefinitionsRestController;
import cz.muni.ics.kypo.training.rest.integration.config.DBTestUtil;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cz.muni.ics.kypo.commons.security.enums.OIDCItems.*;
import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertJsonBytesToObject;
import static cz.muni.ics.kypo.training.rest.controllers.util.ObjectConverter.convertObjectToJsonBytes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
        TrainingDefinitionsRestController.class,
        IntegrationTestApplication.class,
        TestDataFactory.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class TrainingDefinitionsIT {

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private MockMvc mvc;
    private static final Logger logger = LoggerFactory.getLogger(TrainingDefinitionsRestController.class);

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TrainingDefinitionsRestController trainingDefinitionsRestController;
    @Autowired
    private LevelMapperImpl levelMapper;
    @Autowired
    private TrainingDefinitionRepository trainingDefinitionRepository;
    @Autowired
    private UserRefRepository userRefRepository;
    @Autowired
    private TrainingLevelRepository trainingLevelRepository;
    @Autowired
    private InfoLevelRepository infoLevelRepository;
    @Autowired
    private AssessmentLevelRepository assessmentLevelRepository;
    @Autowired
    private TrainingInstanceRepository trainingInstanceRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    @Qualifier("objMapperRESTApi")
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("userManagementExchangeFunction")
    private ExchangeFunction exchangeFunction;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Autowired
    private TrainingDefinitionMapperImpl trainingDefinitionMapper;

    private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO, invalidDefinitionUpdateDTO;
    private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO;
    private TrainingDefinitionByIdDTO invalidDefinitionDTO;
    private TrainingDefinition releasedTrainingDefinition, unreleasedTrainingDefinition, archivedTrainingDefinition;
    private TrainingDefinitionDTO releasedTrainingDefinitionDTO, unreleasedTrainingDefinitionDTO, archivedTrainingDefinitionDTO;
    private TrainingDefinitionInfoDTO releasedTrainingDefinitionInfoDTO, unreleasedTrainingDefinitionInfoDTO, archivedTrainingDefinitionInfoDTO;
    private TrainingInstance trainingInstance;
    private UserRef organizer1, organizer2, author1, author2;
    private TrainingLevel trainingLevel1, trainingLevel2;
    private TrainingLevelUpdateDTO trainingLevelUpdateDTO, invalidTrainingLevelUpdateDTO;
    private InfoLevel infoLevel1;
    private InfoLevelUpdateDTO infoLevelUpdateDTO, invalidInfoLevelUpdateDTO;
    private AssessmentLevel assessmentLevelWithoutQuestions, assessmentLevelWithQuestions;
    private AssessmentLevelUpdateDTO assessmentLevelUpdateDTO, invalidAssessmentLevelUpdateDTO;
    private UserRefDTO organizerDTO1, organizerDTO2, authorDTO1, authorDTO2;
    private MitreTechnique mitreTechnique1, mitreTechnique2;
    private MitreTechniqueDTO mitreTechniqueDTO1, mitreTechniqueDTO2;

    @BeforeEach
    public void init() {
        this.mvc = MockMvcBuilders.standaloneSetup(trainingDefinitionsRestController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new QuerydslPredicateArgumentResolver(
                                new QuerydslBindingsFactory(SimpleEntityPathResolver.INSTANCE), Optional.empty()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .setControllerAdvice(new CustomRestExceptionHandlerTraining())
                .build();

        organizer1 = testDataFactory.getUserRef1();
        organizer2 = testDataFactory.getUserRef2();
        author1 = testDataFactory.getUserRef3();
        author2 = testDataFactory.getUserRef4();
        userRefRepository.saveAll(List.of(organizer1, organizer2, author1, author2));


        organizerDTO1 = testDataFactory.getUserRefDTO1();
        organizerDTO2 = testDataFactory.getUserRefDTO2();
        authorDTO1 = testDataFactory.getUserRefDTO3();
        authorDTO2 = testDataFactory.getUserRefDTO4();

        trainingLevel1 = testDataFactory.getPenalizedLevel();
        trainingLevelUpdateDTO = testDataFactory.getTrainingLevelUpdateDTO();
        invalidTrainingLevelUpdateDTO = new TrainingLevelUpdateDTO();
        trainingLevel2 = testDataFactory.getNonPenalizedLevel();

        infoLevel1 = testDataFactory.getInfoLevel1();
        infoLevelUpdateDTO = testDataFactory.getInfoLevelUpdateDTO();
        invalidInfoLevelUpdateDTO = new InfoLevelUpdateDTO();

        assessmentLevelWithoutQuestions = testDataFactory.getQuestionnaire();
        this.createAssessmentLevelWithQuestions();
        this.createAssessmentLevelUpdateDTO();
        invalidAssessmentLevelUpdateDTO = new AssessmentLevelUpdateDTO();

        trainingDefinitionCreateDTO = testDataFactory.getTrainingDefinitionCreateDTO();

        invalidDefinitionDTO = new TrainingDefinitionByIdDTO();

        releasedTrainingDefinition = testDataFactory.getReleasedDefinition();
        releasedTrainingDefinition.setAuthors(new HashSet<>(List.of(author1)));
        unreleasedTrainingDefinition = testDataFactory.getUnreleasedDefinition();
        unreleasedTrainingDefinition.setAuthors(new HashSet<>(Collections.singletonList(author1)));
        archivedTrainingDefinition = testDataFactory.getArchivedDefinition();
        archivedTrainingDefinition.setAuthors(Set.of(author1));

        releasedTrainingDefinitionInfoDTO = testDataFactory.getReleasedDefinitionInfoDTO();
        unreleasedTrainingDefinitionInfoDTO = testDataFactory.getUnreleasedDefinitionInfoDTO();
        archivedTrainingDefinitionInfoDTO = testDataFactory.getArchivedDefinitionInfoDTO();

        unreleasedTrainingDefinitionDTO = testDataFactory.getUnreleasedDefinitionDTO();
        unreleasedTrainingDefinitionDTO.setCanBeArchived(true);
        releasedTrainingDefinitionDTO = testDataFactory.getReleasedDefinitionDTO();
        releasedTrainingDefinitionDTO.setCanBeArchived(true);
        archivedTrainingDefinitionDTO = testDataFactory.getArchivedDefinitionDTO();
        archivedTrainingDefinitionDTO.setCanBeArchived(true);
        trainingDefinitionUpdateDTO = testDataFactory.getTrainingDefinitionUpdateDTO();
        invalidDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();

        trainingInstance = testDataFactory.getOngoingInstance();
        trainingInstance.setOrganizers(Set.of(author1));

        mitreTechnique1 = testDataFactory.getMitreTechnique1();
        mitreTechnique2 = testDataFactory.getMitreTechnique2();
        mitreTechniqueDTO1 = testDataFactory.getMitreTechniqueDTO1();
        mitreTechniqueDTO2 = testDataFactory.getMitreTechniqueDTO2();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void createAssessmentLevelWithQuestions() {
        assessmentLevelWithQuestions = testDataFactory.getTest();
        entityManager.persist(assessmentLevelWithQuestions);
        Question freeFormQuestion = testDataFactory.getFreeFormQuestion();
        freeFormQuestion.setAssessmentLevel(assessmentLevelWithQuestions);
        entityManager.persist(freeFormQuestion);
        freeFormQuestion.setChoices(testDataFactory.getQuestionChoices(3, "Free form option", Collections.nCopies(3, true), freeFormQuestion));

        Question multipleChoiceQuestion = testDataFactory.getMultipleChoiceQuestion();
        entityManager.persist(multipleChoiceQuestion);
        multipleChoiceQuestion.setChoices(testDataFactory.getQuestionChoices(3, "Multiple choice option", List.of(true, false, true), multipleChoiceQuestion));

        Question extendedMatchingItemsQuestion = testDataFactory.getExtendedMatchingStatements();
        entityManager.persist(extendedMatchingItemsQuestion);

        List<ExtendedMatchingOption> extendedMatchingOptions = testDataFactory.getExtendedMatchingOptions(4, "Option ", extendedMatchingItemsQuestion);
        extendedMatchingItemsQuestion.setExtendedMatchingOptions(extendedMatchingOptions);
        extendedMatchingItemsQuestion.setExtendedMatchingStatements(testDataFactory.getExtendedMatchingStatements(3, "Statement ", extendedMatchingItemsQuestion));
        AtomicInteger index = new AtomicInteger();
        extendedMatchingItemsQuestion.getExtendedMatchingStatements().forEach(item -> item.setExtendedMatchingOption(extendedMatchingOptions.get(index.getAndIncrement() % 4)));
        assessmentLevelWithQuestions.setQuestions(new ArrayList<>(List.of(freeFormQuestion, multipleChoiceQuestion, extendedMatchingItemsQuestion)));
    }

    private void createAssessmentLevelUpdateDTO() {
        assessmentLevelUpdateDTO = testDataFactory.getAssessmentLevelUpdateDTO();
        QuestionDTO freeFormQuestionDTO = testDataFactory.getFreeFormQuestionDTO();
        freeFormQuestionDTO.setChoices(testDataFactory.getQuestionChoiceDTOs(2, "Free form option update", Collections.nCopies(2, true)));

        QuestionDTO multipleChoiceQuestionDTO = testDataFactory.getMultipleChoiceQuestionDTO();
        multipleChoiceQuestionDTO.setChoices(testDataFactory.getQuestionChoiceDTOs(4, "Multiple choice option upudate", List.of(true, false, false, false)));

        QuestionDTO extendedMatchingItemsQuestionDTO = testDataFactory.getExtendedMatchingItemsDTO();
        List<ExtendedMatchingOptionDTO> extendedMatchingOptionDTOs = testDataFactory.getExtendedMatchingOptionDTOs(4, "Option ");
        extendedMatchingItemsQuestionDTO.setExtendedMatchingOptions(extendedMatchingOptionDTOs);
        extendedMatchingItemsQuestionDTO.setExtendedMatchingStatements(testDataFactory.getExtendedMatchingItemDTOs(3, "Statement ", List.of(3, 1, 2)));
        assessmentLevelUpdateDTO.setQuestions(new ArrayList<>(List.of(freeFormQuestionDTO, multipleChoiceQuestionDTO, extendedMatchingItemsQuestionDTO)));
    }

    @AfterEach
    public void reset() throws Exception {
        DBTestUtil.resetAutoIncrementColumns(applicationContext, "training_definition", "abstract_level");
    }

    @Test
    public void findTrainingDefinitionById() throws Exception {
        TrainingDefinition expected = trainingDefinitionRepository.save(releasedTrainingDefinition);
        TrainingLevel trainingLevel = trainingLevelRepository.save(trainingLevel1);
        trainingLevel.setTrainingDefinition(expected);
        trainingDefinitionRepository.save(expected);

        MockHttpServletResponse result = mvc.perform(get("/training-definitions" + "/{id}", expected.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        TrainingDefinitionByIdDTO definitionDTO = trainingDefinitionMapper.mapToDTOById(expected);
        TrainingLevelDTO trainingLevelDTO = levelMapper.mapToTrainingLevelDTO(trainingLevel1);
        trainingLevelDTO.setLevelType(LevelType.TRAINING_LEVEL);
        definitionDTO.setLevels(new ArrayList<>(Collections.singleton(trainingLevelDTO)));
        assertEquals(definitionDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), TrainingDefinitionByIdDTO.class));
    }

    @Test
    public void findTrainingDefinitionById_NotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-definitions" + "/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void findAllTrainingDefinitions_AdminRole() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR.name()));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionDTO>>() {});
        releasedTrainingDefinitionDTO.setId(releasedTrainingDefinition.getId());
        unreleasedTrainingDefinitionDTO.setId(unreleasedTrainingDefinition.getId());
        archivedTrainingDefinitionDTO.setId(archivedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().containsAll(Set.of(releasedTrainingDefinitionDTO, unreleasedTrainingDefinitionDTO, archivedTrainingDefinitionDTO)));
    }

    @Test
    public void findAllTrainingDefinitions() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(Set.of(author1, author2, organizer1));
        unreleasedTrainingDefinition.setAuthors(Set.of(organizer1, organizer2));
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name()));
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionDTO>>() {});
        releasedTrainingDefinitionDTO.setId(releasedTrainingDefinition.getId());
        unreleasedTrainingDefinitionDTO.setId(unreleasedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().containsAll(Set.of(releasedTrainingDefinitionDTO, unreleasedTrainingDefinitionDTO)));
    }

    @Test
    public void findAllTrainingDefinitionsForOrganizers_Released() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(Set.of(author1, author2, organizer1));
        unreleasedTrainingDefinition.setAuthors(Set.of(organizer1, organizer2));
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name()));
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions")
                .queryParam("state", "RELEASED"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {});
        releasedTrainingDefinitionInfoDTO.setId(releasedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().contains(releasedTrainingDefinitionInfoDTO));
    }

    @Test
    public void findAllTrainingDefinitionsForOrganizers_Unreleased_Admin() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(Set.of(author1, author2, organizer1));
        unreleasedTrainingDefinition.setAuthors(Set.of(organizer2));
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR.name()));
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions")
                .queryParam("state", "UNRELEASED"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {});
        unreleasedTrainingDefinitionInfoDTO.setId(unreleasedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().contains(unreleasedTrainingDefinitionInfoDTO));
    }

    @Test
    public void findAllTrainingDefinitionsForOrganizers_Unreleased_OrganizerAndDesigner() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(Set.of(author1, author2, organizer1));
        unreleasedTrainingDefinition.setAuthors(Set.of(organizer1));
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name(), RoleTypeSecurity.ROLE_TRAINING_ORGANIZER.name()));
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        MockHttpServletResponse result = mvc.perform(get("/training-definitions")
                .queryParam("state", "UNRELEASED"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        PageResultResource<TrainingDefinitionInfoDTO> trainingDefinitionsPage = convertJsonBytesToObject(convertJsonBytesToObject(
                result.getContentAsString()), new TypeReference<PageResultResource<TrainingDefinitionInfoDTO>>() {});
        unreleasedTrainingDefinitionInfoDTO.setId(unreleasedTrainingDefinition.getId());
        assertTrue(trainingDefinitionsPage.getContent().contains(unreleasedTrainingDefinitionInfoDTO));
    }

    @Test
    public void createTrainingDefinition() throws Exception {
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name()));
        given(exchangeFunction.exchange(any(ClientRequest.class))).willReturn(buildMockResponse(organizerDTO1));

        MockHttpServletResponse result = mvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionCreateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Optional<TrainingDefinition> newDefinition = trainingDefinitionRepository.findById(1L);
        assertTrue(newDefinition.isPresent());
        TrainingDefinitionByIdDTO newDefinitionDTO = trainingDefinitionMapper.mapToDTOById(newDefinition.get());
        assertEquals(newDefinitionDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), TrainingDefinitionByIdDTO.class));
    }

    @Test
    public void createTrainingDefinition_InvalidDefinition() throws Exception {
        Exception ex = mvc.perform(post("/training-definitions").content(convertObjectToJsonBytes(invalidDefinitionDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateTrainingDefinition() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        userRefRepository.save(author1);
        unreleasedTrainingDefinition.addAuthor(author1);
        trainingDefinitionUpdateDTO.setId(unreleasedTrainingDefinition.getId());
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        Optional<TrainingDefinition> optionalDefinition = trainingDefinitionRepository.findById(unreleasedTrainingDefinition.getId());
        assertTrue(optionalDefinition.isPresent());
        TrainingDefinition updatedDefinition = optionalDefinition.get();
        assertEquals(trainingDefinitionUpdateDTO.getTitle(), updatedDefinition.getTitle());
        assertEquals(trainingDefinitionUpdateDTO.getDescription(), updatedDefinition.getDescription());
        assertEquals(trainingDefinitionUpdateDTO.getState().toString(), updatedDefinition.getState().toString());
        assertEquals(unreleasedTrainingDefinition.getAuthors(), updatedDefinition.getAuthors());
    }

    @Test
    public void updateTrainingDefinition_InvalidDefinition() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(invalidDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateTrainingDefinition_DefinitionNotFound() throws Exception {
        trainingDefinitionUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void updateTrainingDefinition_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingDefinitionUpdateDTO.setId(releasedTrainingDefinition.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateTrainingDefinition_CreatedInstance() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingInstance.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingInstanceRepository.save(trainingInstance);
        trainingDefinitionUpdateDTO.setId(unreleasedTrainingDefinition.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions").content(convertObjectToJsonBytes(trainingDefinitionUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", unreleasedTrainingDefinition.getId().toString(),
                "Cannot update training definition with already created training instance. Remove training instance/s before updating training definition.");
    }

    @Test
    public void cloneTrainingDefinition() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);
        mockSpringSecurityContextForGet(List.of(RoleTypeSecurity.ROLE_TRAINING_DESIGNER.name()));
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionMapper.mapToDTOById(unreleasedTrainingDefinition);
        trainingDefinitionByIdDTO.setLevels(List.of(levelMapper.mapToDTO(trainingLevel1)));

        MockHttpServletResponse result = mvc.perform(post("/training-definitions" + "/{id}", unreleasedTrainingDefinition.getId())
                .param("title", "title"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse();
        Optional<TrainingDefinition> clonedTrainingDefinition = trainingDefinitionRepository.findById(2L);
        assertTrue(clonedTrainingDefinition.isPresent());
        assertEquals("title", clonedTrainingDefinition.get().getTitle());
        assertEquals(clonedTrainingDefinition.get().getState().toString(), TDState.UNRELEASED.toString());
        assertNull(clonedTrainingDefinition.get().getBetaTestingGroup());
    }

    @Test
    public void cloneNonexistentTrainingDefinition() throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/training-definitions" + "/{id}", 100L).param("title", "title"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void swapLevels() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setOrder(1);
        trainingLevel2.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevel2.setOrder(2);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevel1.setOrder(3);
        trainingLevelRepository.saveAll(Set.of(trainingLevel1, trainingLevel2));
        infoLevelRepository.save(infoLevel1);

        mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedTrainingDefinition.getId(), infoLevel1.getId(), trainingLevel1.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        Optional<TrainingLevel> optTrainingLevel = trainingLevelRepository.findById(trainingLevel1.getId());
        Optional<InfoLevel> optInfoLevel = infoLevelRepository.findById(infoLevel1.getId());
        assertTrue(optTrainingLevel.isPresent());
        assertTrue(optInfoLevel.isPresent());
        assertEquals(1, trainingLevel1.getOrder());
        assertEquals(3, infoLevel1.getOrder());
    }

    @Test
    public void swapLevels_FromLevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setOrder(1);
        infoLevelRepository.save(infoLevel1);

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedTrainingDefinition.getId(), 50, infoLevel1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "50",
                "Level not found.");
    }

    @Test
    public void swapLevels_ToLevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setOrder(1);
        infoLevelRepository.save(infoLevel1);

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedTrainingDefinition.getId(), infoLevel1.getId(), 50))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "50",
                "Level not found.");
    }

    @Test
    public void swapLevels_CreatedInstance() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingInstance.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingInstanceRepository.save(trainingInstance);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/levels/{levelIdFrom}/swap-with/{levelIdTo}",
                unreleasedTrainingDefinition.getId(), trainingLevel1.getId(), infoLevel1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", unreleasedTrainingDefinition.getId().toString(),
                "Cannot update training definition with already created training instance. Remove training instance/s before updating training definition.");
    }

    @Test
    public void deleteTrainingDefinition() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assessmentLevelWithoutQuestions.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevelWithoutQuestions);
        trainingLevelRepository.save(trainingLevel1);
        infoLevelRepository.save(infoLevel1);

        mvc.perform(delete("/training-definitions" + "/{id}", unreleasedTrainingDefinition.getId()))
                .andExpect(status().isOk());
        assertFalse(trainingDefinitionRepository.findById(unreleasedTrainingDefinition.getId()).isPresent());
        assertFalse(assessmentLevelRepository.findById(assessmentLevelWithoutQuestions.getId()).isPresent());
        assertFalse(trainingLevelRepository.findById(trainingLevel1.getId()).isPresent());
        assertFalse(infoLevelRepository.findById(infoLevel1.getId()).isPresent());
    }

    @Test
    public void deleteDefinition_NotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void deleteTrainingDefinition_Released() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{Id}", releasedTrainingDefinition.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot delete released training definition.");
    }

    @Test
    public void deleteDefinition_WithTrainingInstances() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingInstance.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingInstanceRepository.save(trainingInstance);

        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{id}", unreleasedTrainingDefinition.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", unreleasedTrainingDefinition.getId().toString(),
                "Cannot delete training definition with already created training instance. Remove training instance/s before deleting training definition.");
    }

    @Test
    public void deleteOneLevel() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);
        infoLevelRepository.save(infoLevel1);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);

        mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", unreleasedTrainingDefinition.getId(), trainingLevel1.getId()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        assertTrue(trainingDefinitionRepository.findById(unreleasedTrainingDefinition.getId()).isPresent());
        assertFalse(trainingLevelRepository.findById(trainingLevel1.getId()).isPresent());
        assertTrue(infoLevelRepository.findById(infoLevel1.getId()).isPresent());
    }

    @Test
    public void deleteOneLevel_NotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", unreleasedTrainingDefinition.getId(), 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "100",
                "Level not found.");
    }

    @Test
    public void deleteOneLevel_DefinitionNotFound() throws Exception {
        trainingLevelRepository.save(trainingLevel1);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}", 100L, trainingLevel1.getId()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void deleteOneLevel_ReleasedDefinition() throws Exception {
        trainingLevel1.setTrainingDefinition(releasedTrainingDefinition);
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}",
                releasedTrainingDefinition.getId(), trainingLevel1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", trainingLevel1.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void deleteOneLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        trainingLevel1.setTrainingDefinition(archivedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);
        MockHttpServletResponse response = mvc.perform(delete("/training-definitions/{definitionId}/levels/{levelId}",
                archivedTrainingDefinition.getId(), trainingLevel1.getId()))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", trainingLevel1.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateTrainingLevel() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevelUpdateDTO.setId(trainingLevel1.getId());

        mvc.perform(put("/training-definitions/{definitionId}/training-levels", unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(trainingLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<TrainingLevel> updatedTrainingLevel = trainingLevelRepository.findById(trainingLevel1.getId());
        assertTrue(updatedTrainingLevel.isPresent());
        assertEquals(updatedTrainingLevel.get().getTitle(), trainingLevelUpdateDTO.getTitle());
        assertEquals(updatedTrainingLevel.get().getContent(), trainingLevelUpdateDTO.getContent());
        assertEquals(updatedTrainingLevel.get().getAnswer(), trainingLevelUpdateDTO.getAnswer());
        assertEquals(updatedTrainingLevel.get().getSolution(), trainingLevelUpdateDTO.getSolution());
        assertEquals(updatedTrainingLevel.get().isSolutionPenalized(), trainingLevelUpdateDTO.isSolutionPenalized());
        assertEquals(updatedTrainingLevel.get().getMaxScore(), trainingLevelUpdateDTO.getMaxScore());
    }

    @Test
    public void updateTrainingLevelAddNewMitreTechniques() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevelUpdateDTO.setId(trainingLevel1.getId());
        doReturn(buildMockResponse(authorDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        trainingLevelUpdateDTO.setMitreTechniques(List.of(mitreTechniqueDTO1, mitreTechniqueDTO2));

        mvc.perform(put("/training-definitions/{definitionId}/training-levels", unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(trainingLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        Optional<TrainingLevel> updatedTrainingLevel = trainingLevelRepository.findById(trainingLevel1.getId());
        assertTrue(updatedTrainingLevel.isPresent());
        assertEquals(2, updatedTrainingLevel.get().getMitreTechniques().size());
    }

    @Test
    public void updateTrainingLevelUpdateMitreTechniques() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingLevel1.addMitreTechnique(mitreTechnique1);
        trainingLevel1.addMitreTechnique(mitreTechnique2);
        trainingLevelRepository.save(trainingLevel1);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevelUpdateDTO.setId(trainingLevel1.getId());
        doReturn(buildMockResponse(authorDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        MitreTechniqueDTO mitreTechniqueDTO = new MitreTechniqueDTO();
        mitreTechniqueDTO.setId(mitreTechnique1.getId());
        mitreTechniqueDTO.setTechniqueKey(mitreTechnique1.getTechniqueKey());
        trainingLevelUpdateDTO.setMitreTechniques(List.of(mitreTechniqueDTO1, mitreTechniqueDTO2, mitreTechniqueDTO));

        mvc.perform(put("/training-definitions/{definitionId}/training-levels", unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(trainingLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<TrainingLevel> updatedTrainingLevel = trainingLevelRepository.findById(trainingLevel1.getId());
        assertTrue(updatedTrainingLevel.isPresent());
        assertEquals(3, updatedTrainingLevel.get().getMitreTechniques().size());
        assertFalse(updatedTrainingLevel.get().getMitreTechniques().contains(mitreTechnique2));
        assertTrue(mitreTechnique2.getTrainingLevels().isEmpty());
    }

    @Test
    public void updateTrainingLevelRemoveAllMitreTechniques() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingLevel1.addMitreTechnique(mitreTechnique1);
        trainingLevel1.addMitreTechnique(mitreTechnique2);
        trainingLevelRepository.save(trainingLevel1);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevelUpdateDTO.setId(trainingLevel1.getId());
        doReturn(buildMockResponse(authorDTO1)).when(exchangeFunction).exchange(any(ClientRequest.class));

        mvc.perform(put("/training-definitions/{definitionId}/training-levels", unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(trainingLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<TrainingLevel> updatedTrainingLevel = trainingLevelRepository.findById(trainingLevel1.getId());
        assertTrue(updatedTrainingLevel.isPresent());
        assertEquals(0, updatedTrainingLevel.get().getMitreTechniques().size());
        assertFalse(updatedTrainingLevel.get().getMitreTechniques().contains(mitreTechnique1));
        assertFalse(updatedTrainingLevel.get().getMitreTechniques().contains(mitreTechnique2));
        assertTrue(mitreTechnique1.getTrainingLevels().isEmpty());
        assertTrue(mitreTechnique2.getTrainingLevels().isEmpty());
    }

    @Test
    public void updateTrainingLevel_InvalidLevel() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/training-levels", 100L).content(convertObjectToJsonBytes(invalidTrainingLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateTrainingLevel_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);
        trainingLevelUpdateDTO.setId(trainingLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/training-levels",
                releasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(trainingLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateTrainingLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);
        trainingLevelUpdateDTO.setId(trainingLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/training-levels", archivedTrainingDefinition.getId()).content(convertObjectToJsonBytes(trainingLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateTrainingLevel_DefinitionNotFound() throws Exception {
        trainingLevelUpdateDTO.setId(1L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/training-levels", 100L).content(convertObjectToJsonBytes(trainingLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void updateTrainingLevel_NotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingLevelUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/training-levels",
                unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(trainingLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", trainingLevelUpdateDTO.getId().toString(),
                "Level not found.");
    }

    @Test
    public void updateInfoLevel() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevelUpdateDTO.setId(infoLevel1.getId());

        mvc.perform(put("/training-definitions/{definitionId}/info-levels", unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        Optional<InfoLevel> updatedInfoLevel = infoLevelRepository.findById(infoLevel1.getId());
        assertTrue(updatedInfoLevel.isPresent());
        assertEquals(updatedInfoLevel.get().getTitle(), infoLevelUpdateDTO.getTitle());
        assertEquals(updatedInfoLevel.get().getContent(), infoLevelUpdateDTO.getContent());
    }

    @Test
    public void updateInfoLevel_InvalidLevel() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/info-levels", 100L).content(convertObjectToJsonBytes(invalidInfoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateInfoLevel_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        infoLevelUpdateDTO.setId(infoLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels", releasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateInfoLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        infoLevelUpdateDTO.setId(infoLevel1.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels",
                archivedTrainingDefinition.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateInfoLevel_DefinitionNotFound() throws Exception {
        infoLevelUpdateDTO.setId(1L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels", 100L).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void updateInfoLevel_LevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevelUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/info-levels",
                unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(infoLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", infoLevelUpdateDTO.getId().toString(),
                "Level not found.");

    }

    @Test
    public void updateAssessmentLevelWithoutQuestions() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevelWithoutQuestions);
        assessmentLevelWithoutQuestions.setTrainingDefinition(unreleasedTrainingDefinition);
        assessmentLevelUpdateDTO.setId(assessmentLevelWithoutQuestions.getId());
        assertTrue(assessmentLevelWithoutQuestions.getQuestions().isEmpty());

        mvc.perform(put("/training-definitions/{definitionId}/assessment-levels",
                unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        Optional<AssessmentLevel> updatedAssessmentLevel = assessmentLevelRepository.findById(assessmentLevelWithoutQuestions.getId());
        assertTrue(updatedAssessmentLevel.isPresent());
        assertEquals(updatedAssessmentLevel.get().getTitle(), assessmentLevelUpdateDTO.getTitle());
        assertEquals(updatedAssessmentLevel.get().getAssessmentType().toString(), assessmentLevelUpdateDTO.getType().toString());
        assertEquals(assessmentLevelUpdateDTO.getQuestions().size(), updatedAssessmentLevel.get().getQuestions().size());
        assertEquals(updatedAssessmentLevel.get().getInstructions(), assessmentLevelUpdateDTO.getInstructions());
        assertEquals(updatedAssessmentLevel.get().getMaxScore(), assessmentLevelUpdateDTO.getQuestions().stream().mapToInt(QuestionDTO::getPoints).sum());
        this.assertQuestionAfterUpdate(updatedAssessmentLevel.get());
    }

    @Test
    public void updateAssessmentLevelWithQuestions() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assessmentLevelWithQuestions.setTrainingDefinition(unreleasedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevelWithQuestions);
        assessmentLevelUpdateDTO.setId(assessmentLevelWithQuestions.getId());
        assertFalse(assessmentLevelWithQuestions.getQuestions().isEmpty());

        assessmentLevelWithQuestions.getQuestions().forEach(question -> {
            if (question.getQuestionType() == cz.muni.ics.kypo.training.persistence.model.enums.QuestionType.FFQ) {
                assertEquals(3, question.getChoices().size());
            } else if (question.getQuestionType() == cz.muni.ics.kypo.training.persistence.model.enums.QuestionType.MCQ) {
                assertEquals(3, question.getChoices().size());
            } else if (question.getQuestionType() == cz.muni.ics.kypo.training.persistence.model.enums.QuestionType.EMI) {
                assertEquals(4, question.getExtendedMatchingOptions().size());
                assertEquals(3, question.getExtendedMatchingStatements().size());
                question.getExtendedMatchingStatements().forEach(Assertions::assertNotNull);
            }
        });

        mvc.perform(put("/training-definitions/{definitionId}/assessment-levels",
                unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        Optional<AssessmentLevel> updatedAssessmentLevel = assessmentLevelRepository.findById(assessmentLevelWithQuestions.getId());
        assertTrue(updatedAssessmentLevel.isPresent());
        assertEquals(updatedAssessmentLevel.get().getTitle(), assessmentLevelUpdateDTO.getTitle());
        assertEquals(updatedAssessmentLevel.get().getAssessmentType().toString(), assessmentLevelUpdateDTO.getType().toString());
        assertEquals(assessmentLevelUpdateDTO.getQuestions().size(), updatedAssessmentLevel.get().getQuestions().size());
        assertEquals(updatedAssessmentLevel.get().getInstructions(), assessmentLevelUpdateDTO.getInstructions());
        assertEquals(updatedAssessmentLevel.get().getMaxScore(), assessmentLevelUpdateDTO.getQuestions().stream().mapToInt(QuestionDTO::getPoints).sum());
        this.assertQuestionAfterUpdate(updatedAssessmentLevel.get());
    }

    @Test
    public void updateAssessmentLevel_InvalidLevel() throws Exception {
        Exception ex = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", 100L).content(convertObjectToJsonBytes(invalidAssessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException();
        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
    }

    @Test
    public void updateAssessmentLevel_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevelWithoutQuestions);
        assessmentLevelUpdateDTO.setId(assessmentLevelWithoutQuestions.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", releasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateAssessmentLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevelWithoutQuestions);
        assessmentLevelUpdateDTO.setId(assessmentLevelWithoutQuestions.getId());

        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels",
                archivedTrainingDefinition.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void updateAssessmentLevel_DefinitionNotFound() throws Exception {
        assessmentLevelUpdateDTO.setId(1L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", 100L).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void updateAssessmentLevel_LevelNotFound() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assessmentLevelUpdateDTO.setId(100L);
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/assessment-levels", unreleasedTrainingDefinition.getId()).content(convertObjectToJsonBytes(assessmentLevelUpdateDTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", assessmentLevelUpdateDTO.getId().toString(),
                "Level not found.");
    }

    @Test
    public void findTrainingLevelById() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        trainingLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        trainingLevelRepository.save(trainingLevel1);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", trainingLevel1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        TrainingLevelDTO trainingLevelDTO = levelMapper.mapToTrainingLevelDTO(trainingLevel1);
        trainingLevelDTO.setLevelType(LevelType.TRAINING_LEVEL);
        assertEquals(trainingLevelDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), TrainingLevelDTO.class));
    }

    @Test
    public void findInfoLevelById() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        infoLevel1.setTrainingDefinition(unreleasedTrainingDefinition);
        infoLevelRepository.save(infoLevel1);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", infoLevel1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        InfoLevelDTO infoLevelDTO = levelMapper.mapToInfoLevelDTO(infoLevel1);
        infoLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        assertEquals(infoLevelDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), InfoLevelDTO.class));
    }

    @Test
    public void findAssessmentLevelById() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assessmentLevelWithoutQuestions.setTrainingDefinition(unreleasedTrainingDefinition);
        assessmentLevelRepository.save(assessmentLevelWithoutQuestions);
        MockHttpServletResponse result = mvc.perform(get("/training-definitions/levels/{id}", assessmentLevelWithoutQuestions.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        AssessmentLevelDTO assessmentLevelDTO = levelMapper.mapToAssessmentLevelDTO(assessmentLevelWithoutQuestions);
        assessmentLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        assertEquals(assessmentLevelDTO, convertJsonBytesToObject(convertJsonBytesToObject(result.getContentAsString()), AssessmentLevelDTO.class));
    }

    @Test
    public void findTrainingLevelById_NotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(get("/training-definitions/levels/{id}", 100L))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), AbstractLevel.class, "id", "100",
                "Level not found.");
    }

    @Test
    public void createTrainingLevel() throws Exception {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.TRAINING))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        BasicLevelInfoDTO levelInfoDTO = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), BasicLevelInfoDTO.class);
        Optional<TrainingLevel> optionalTrainingLevel = trainingLevelRepository.findById(levelInfoDTO.getId());
        assertTrue(optionalTrainingLevel.isPresent());
        TrainingLevel trainingLevel = optionalTrainingLevel.get();
        assertEquals(100, trainingLevel.getMaxScore() );
        assertEquals("Title of training level", trainingLevel.getTitle());
        assertEquals(100, trainingLevel.getIncorrectAnswerLimit());
        assertEquals("Secret answer", trainingLevel.getAnswer());
        assertTrue(trainingLevel.isSolutionPenalized());
        assertEquals("Solution of the training should be here", trainingLevel.getSolution());
        assertEquals("The test entry should be here", trainingLevel.getContent());
    }

    @Test
    public void createInfoLevel() throws Exception {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.INFO))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        BasicLevelInfoDTO levelInfoDTO = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), BasicLevelInfoDTO.class);
        Optional<InfoLevel> optionalInfoLevel = infoLevelRepository.findById(levelInfoDTO.getId());
        assertTrue(optionalInfoLevel.isPresent());
        InfoLevel infoLevel = optionalInfoLevel.get();
        assertEquals(0, infoLevel.getMaxScore());
        assertEquals("Title of info level", infoLevel.getTitle());
        assertEquals("Content of info level should be here.", infoLevel.getContent());
    }

    @Test
    public void createAssessmentLevel() throws Exception {
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", trainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        BasicLevelInfoDTO levelInfoDTO = convertJsonBytesToObject(convertJsonBytesToObject(response.getContentAsString()), BasicLevelInfoDTO.class);
        Optional<AssessmentLevel> optionalAssessmentLevel = assessmentLevelRepository.findById(levelInfoDTO.getId());
        assertTrue(optionalAssessmentLevel.isPresent());
        AssessmentLevel assessmentLevel = optionalAssessmentLevel.get();
        assertEquals(0, assessmentLevel.getMaxScore());
        assertEquals("Title of assessment level", assessmentLevel.getTitle());
        assertEquals(AssessmentType.QUESTIONNAIRE.toString(), assessmentLevel.getAssessmentType().toString());
        assertEquals("Instructions should be here", assessmentLevel.getInstructions());
    }

    @Test
    public void createLevel_ReleasedDefinition() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void createLevel_ArchivedDefinition() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", archivedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot edit released or archived training definition.");
    }

    @Test
    public void createLevel_DefinitionNotFound() throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/training-definitions/{definitionId}/levels/{levelType}", 100L, cz.muni.ics.kypo.training.persistence.model.enums.LevelType.ASSESSMENT))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "100",
                "Entity TrainingDefinition (id: 100) not found.");
    }

    @Test
    public void editAuthors_Add() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assertTrue(releasedTrainingDefinition.getAuthors().size() == 1 && releasedTrainingDefinition.getAuthors().contains(author1));
        ArgumentMatcher<ClientRequest> userInfoMatcher = clientRequest -> clientRequest.url().getPath().equals("/users/info");
        doReturn(buildMockResponse(organizerDTO1)).when(exchangeFunction).exchange(argThat(userInfoMatcher));
        ArgumentMatcher<ClientRequest> usersIdsMatcher = clientRequest -> clientRequest.url().getPath().equals("/users/ids");
        doReturn(buildMockResponse(new PageResultResource<UserRefDTO>(List.of(organizerDTO1, authorDTO1, authorDTO2), new PageResultResource.Pagination(0,3,3,3,1))))
                .when(exchangeFunction).exchange(argThat(usersIdsMatcher));

        mvc.perform(put("/training-definitions/{definitionId}/authors", releasedTrainingDefinition.getId())
                .queryParam("authorsAddition",  StringUtils.collectionToDelimitedString(List.of(author1.getUserRefId(), author2.getUserRefId(), organizer1.getUserRefId()), ","))
                .queryParam("authorsRemoval", ""))
                .andExpect(status().isNoContent());
        assertEquals(3, releasedTrainingDefinition.getAuthors().size());
        assertEquals(Set.of(author1, author2, organizer1), releasedTrainingDefinition.getAuthors());
    }

    @Test
    public void editAuthors_Remove() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(new HashSet<>(Set.of(author1, author2, organizer1, organizer2)));
        ArgumentMatcher<ClientRequest> userInfoMatcher = clientRequest -> clientRequest.url().getPath().equals("/users/info");
        doReturn(buildMockResponse(authorDTO1)).when(exchangeFunction).exchange(argThat(userInfoMatcher));

        mvc.perform(put("/training-definitions/{definitionId}/authors", releasedTrainingDefinition.getId())
                .queryParam("authorsAddition",  "")
                .queryParam("authorsRemoval", StringUtils.collectionToDelimitedString(List.of(organizer2.getUserRefId(), organizer1.getUserRefId()), ",")))
                .andExpect(status().isNoContent());
        assertEquals(2, releasedTrainingDefinition.getAuthors().size());
        assertEquals(Set.of(author1, author2), releasedTrainingDefinition.getAuthors());
    }

    @Test
    public void editAuthors_RemoveLoggedInAuthor() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        releasedTrainingDefinition.setAuthors(new HashSet<>(Set.of(author1, author2)));
        ArgumentMatcher<ClientRequest> userInfoMatcher = clientRequest -> clientRequest.url().getPath().equals("/users/info");
        doReturn(buildMockResponse(authorDTO1)).when(exchangeFunction).exchange(argThat(userInfoMatcher));

        mvc.perform(put("/training-definitions/{definitionId}/authors", releasedTrainingDefinition.getId())
                .queryParam("authorsAddition",  "")
                .queryParam("authorsRemoval", StringUtils.collectionToDelimitedString(List.of(author1.getUserRefId()), ",")))
                .andExpect(status().isNoContent());
        assertEquals(2, releasedTrainingDefinition.getAuthors().size());
        assertEquals(Set.of(author1, author2), releasedTrainingDefinition.getAuthors());
    }

    @Test
    public void switchStateOfDefinition_DefinitionNotFound() throws Exception {
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/states/{state}", 1000, cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", "1000",
                "Entity TrainingDefinition (id: 1000) not found.");
    }

    @Test
    public void switchStateOfDefinition_UnreleasedToReleased() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);
        assertEquals(TDState.UNRELEASED.name(), unreleasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", unreleasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.RELEASED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.RELEASED.name(), unreleasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_UnreleasedToUnreleased() throws Exception {
        trainingDefinitionRepository.save(unreleasedTrainingDefinition);

        assertEquals(TDState.UNRELEASED.name(), unreleasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", unreleasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.UNRELEASED.name(), unreleasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_ReleasedToArchived() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.ARCHIVED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.ARCHIVED.name(), releasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_ReleasedToUnreleased_Allowed() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        mvc.perform(put("/training-definitions/{definitionId}/states/{state}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isNoContent());
        assertEquals(TDState.UNRELEASED.name(), releasedTrainingDefinition.getState().name());
    }

    @Test
    public void switchStateOfDefinition_ReleasedToUnreleased_NotAllowed() throws Exception {
        trainingDefinitionRepository.save(releasedTrainingDefinition);
        trainingInstance.setTrainingDefinition(releasedTrainingDefinition);
        trainingInstanceRepository.save(trainingInstance);

        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/states/{state}", releasedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        assertEquals(TDState.RELEASED.name(), releasedTrainingDefinition.getState().name());
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", releasedTrainingDefinition.getId().toString(),
                "Cannot update training definition with already created training instance(s). Remove training " +
                        "instance(s) before changing the state from released to unreleased training definition.");
    }

    @Test
    public void switchStateOfDefinition_ArchivedToUnreleased() throws Exception {
        trainingDefinitionRepository.save(archivedTrainingDefinition);

        assertEquals(TDState.ARCHIVED.name(), archivedTrainingDefinition.getState().name());
        MockHttpServletResponse response = mvc.perform(put("/training-definitions/{definitionId}/states/{state}",
                archivedTrainingDefinition.getId(), cz.muni.ics.kypo.training.persistence.model.enums.TDState.UNRELEASED))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
        assertEquals(TDState.ARCHIVED.name(), archivedTrainingDefinition.getState().name());
        ApiEntityError error = convertJsonBytesToObject(response.getContentAsString(), ApiEntityError.class);
        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEntityDetailError(error.getEntityErrorDetail(), TrainingDefinition.class, "id", archivedTrainingDefinition.getId().toString(),
                "Cannot switch from " + TDState.ARCHIVED.name() + " to " + TDState.UNRELEASED.name());
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
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return Mono.just(clientResponse);
    }

    private void assertQuestionAfterUpdate(AssessmentLevel updatedAssessmentLevel) {
        assessmentLevelUpdateDTO.getQuestions().forEach(questionDTO -> {
            if (questionDTO.getQuestionType() == QuestionType.FFQ) {
                Optional<Question> freeFormQuestion = updatedAssessmentLevel.getQuestions().stream()
                        .filter(question -> question.getQuestionType() == cz.muni.ics.kypo.training.persistence.model.enums.QuestionType.FFQ)
                        .findFirst();
                assertTrue(freeFormQuestion.isPresent());
                assertEquals(questionDTO.getChoices().size(), freeFormQuestion.get().getChoices().size());
            } else if (questionDTO.getQuestionType() == QuestionType.MCQ) {
                Optional<Question> multipleChoiceQuestion = updatedAssessmentLevel.getQuestions().stream()
                        .filter(question -> question.getQuestionType() == cz.muni.ics.kypo.training.persistence.model.enums.QuestionType.MCQ)
                        .findFirst();
                assertTrue(multipleChoiceQuestion.isPresent());
                assertEquals(questionDTO.getChoices().size(), multipleChoiceQuestion.get().getChoices().size());
            } else {
                Optional<Question> extendedMatchingItemsQuestion = updatedAssessmentLevel.getQuestions().stream()
                        .filter(question -> question.getQuestionType() == cz.muni.ics.kypo.training.persistence.model.enums.QuestionType.EMI)
                        .findFirst();
                assertTrue(extendedMatchingItemsQuestion.isPresent());
                assertEquals(questionDTO.getExtendedMatchingStatements().size(), extendedMatchingItemsQuestion.get().getExtendedMatchingStatements().size());
                assertEquals(questionDTO.getExtendedMatchingOptions().size(), extendedMatchingItemsQuestion.get().getExtendedMatchingOptions().size());
                AtomicInteger index = new AtomicInteger();
                questionDTO.getExtendedMatchingStatements().forEach(emi -> {
                    Integer optionOrder = extendedMatchingItemsQuestion.get().getExtendedMatchingStatements()
                            .get(index.getAndIncrement())
                            .getExtendedMatchingOption()
                            .getOrder();
                    assertEquals(optionOrder, emi.getCorrectOptionOrder());
                });
            }
        });
    }
}

