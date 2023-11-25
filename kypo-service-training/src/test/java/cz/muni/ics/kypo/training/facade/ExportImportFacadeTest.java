package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.muni.ics.kypo.training.api.dto.export.FileToReturnDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.TrainingLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.imports.InfoLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.service.SecurityService;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.service.api.TrainingFeedbackApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.ExportImportService;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.api.SandboxApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {
        TestDataFactory.class,
        ExportImportMapperImpl.class,
        TrainingDefinitionMapperImpl.class,
        LevelMapperImpl.class,
        UserRefMapperImpl.class,
        BetaTestingGroupMapperImpl.class,
        HintMapperImpl.class,
        QuestionMapperImpl.class,
        AttachmentMapperImpl.class,
        ReferenceSolutionNodeMapperImpl.class,
        MitreTechniqueMapperImpl.class
})
public class ExportImportFacadeTest {

    private ExportImportFacade exportImportFacade;

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private ExportImportMapperImpl exportImportMapper;
    @Autowired
    private LevelMapperImpl infoLevelMapper;
    @Autowired
    private TrainingDefinitionMapperImpl trainingDefinitionMapper;

    @MockBean
    private ObjectMapper objectMapper;
    @MockBean
    private TrainingDefinitionService trainingDefinitionService;
    @MockBean
    private SandboxApiService sandboxApiService;
    @MockBean
    private ExportImportService exportImportService;
    @MockBean
    private TrainingFeedbackApiService trainingFeedbackApiService;
    @MockBean
    private UserService userService;
    @MockBean
    private SecurityService securityService;

    private TrainingDefinition trainingDefinition;
    private TrainingDefinition trainingDefinitionImported;
    private AssessmentLevel assessmentLevel;
    private TrainingLevel trainingLevel;
    private InfoLevel infoLevel;
    private ImportTrainingDefinitionDTO importTrainingDefinitionDTO;
    private ElasticsearchApiService elasticsearchApiService;
    private TrainingInstance exportTrainingInstance;
    private TrainingRun[] trainingRuns;
    private UserRefDTO[] userRefDTOS;
    private final String DELIMITER = ";";

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        exportImportFacade = new ExportImportFacade(exportImportService, trainingDefinitionService, elasticsearchApiService,
                trainingFeedbackApiService, sandboxApiService, userService, securityService, exportImportMapper, infoLevelMapper, trainingDefinitionMapper, objectMapper);

        assessmentLevel = testDataFactory.getTest();
        assessmentLevel.setId(1L);

        trainingLevel = testDataFactory.getPenalizedLevel();
        trainingLevel.setId(2L);

        infoLevel = testDataFactory.getInfoLevel1();
        infoLevel.setId(3L);

        AssessmentLevelImportDTO importAssessmentLevelDTO = testDataFactory.getAssessmentLevelImportDTO();
        importAssessmentLevelDTO.setOrder(3);

        TrainingLevelImportDTO importGameLevelDTO = testDataFactory.getTrainingLevelImportDTO();
        importGameLevelDTO.setOrder(2);

        InfoLevelImportDTO importInfoLevelDTO = testDataFactory.getInfoLevelImportDTO();
        importInfoLevelDTO.setOrder(1);

        trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setId(1L);

        trainingDefinitionImported = testDataFactory.getUnreleasedDefinition();
        trainingDefinitionImported.setId(1L);

        importTrainingDefinitionDTO = testDataFactory.getImportTrainingDefinitionDTO();
        importTrainingDefinitionDTO.setLevels(Arrays.asList(importInfoLevelDTO, importGameLevelDTO, importAssessmentLevelDTO));

        TrainingInstance trainingInstance = testDataFactory.getConcludedInstance();
        trainingInstance.setTrainingDefinition(trainingDefinition);

        TrainingRun trainingRun = testDataFactory.getFinishedRun();
        trainingRun.setTrainingInstance(trainingInstance);


        exportTrainingInstance = testDataFactory.getConcludedInstance();
        exportTrainingInstance.setId(18L);
        UserRef user = testDataFactory.getUserRef1();
        UserRef user2 = testDataFactory.getUserRef2();
        trainingRuns = new TrainingRun[2];
        userRefDTOS = new UserRefDTO[2];
        userRefDTOS[0] = testDataFactory.getUserRefDTO1();
        userRefDTOS[1] = testDataFactory.getUserRefDTO2();

        TrainingRun trainingRun2 = testDataFactory.getFinishedRun();
        trainingRuns[0] = trainingRun2;
        trainingRun2.setTrainingInstance(exportTrainingInstance);
        trainingRun2.setTotalTrainingScore(131);
        trainingRun2.setParticipantRef(user);

        TrainingRun trainingRun3 = testDataFactory.getFinishedRun();
        trainingRuns[1] = trainingRun3;
        trainingRun3.setTrainingInstance(exportTrainingInstance);
        trainingRun3.setTotalTrainingScore(10);
        trainingRun3.setParticipantRef(user2);
    }

	@Test
    public void dbExport() throws Exception {
        given(exportImportService.findById(trainingDefinition.getId())).willReturn(trainingDefinition);
        given(trainingDefinitionService.findLevelById(infoLevel.getId())).willReturn(infoLevel);
        given(trainingDefinitionService.findLevelById(trainingLevel.getId())).willReturn(trainingLevel);
        given(trainingDefinitionService.findLevelById(assessmentLevel.getId())).willReturn(assessmentLevel);
        ExportTrainingDefinitionAndLevelsDTO exportedTrainingDefinition = exportImportMapper.mapToDTO(trainingDefinition);
        given(objectMapper.writeValueAsBytes(any(ExportTrainingDefinitionAndLevelsDTO.class))).willReturn(convertObjectToJsonBytes(exportedTrainingDefinition));
        FileToReturnDTO export = exportImportFacade.dbExport(trainingDefinition.getId());

        assertEquals(exportedTrainingDefinition.toString(), convertJsonBytesToString(export.getContent()).toString());
        assertEquals(trainingDefinition.getTitle(), export.getTitle());
    }

    private static ExportTrainingDefinitionAndLevelsDTO convertJsonBytesToString(byte[] object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(object, ExportTrainingDefinitionAndLevelsDTO.class);
    }

    private static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(object);
    }

    @Test
    public void dbImport() {
        given(trainingDefinitionService.create(any(TrainingDefinition.class), any(Boolean.class))).willReturn(trainingDefinitionImported);

        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = exportImportFacade.dbImport(importTrainingDefinitionDTO);
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTOImported = trainingDefinitionMapper.mapToDTOById(trainingDefinitionImported);

        deepEqualsTrainingDefinitionDTO(trainingDefinitionByIdDTOImported, trainingDefinitionByIdDTO);
    }

    @Test
    public void exportUserScoreFromTrainingInstance() {
        given(exportImportService.findRunsByInstanceId(exportTrainingInstance.getId())).willReturn(Arrays.stream(trainingRuns).collect(Collectors.toSet()));
        given(userService.getUserRefDTOByUserRefId(trainingRuns[0].getParticipantRef().getUserRefId())).willReturn(userRefDTOS[0]);
        given(userService.getUserRefDTOByUserRefId(trainingRuns[1].getParticipantRef().getUserRefId())).willReturn(userRefDTOS[1]);

        FileToReturnDTO exportedFile = exportImportFacade.exportUserScoreFromTrainingInstance(exportTrainingInstance.getId());
        String header = "trainingInstanceId;userRefSub;totalTrainingScore" + System.lineSeparator();
        String expectedString = getCSV(trainingRuns[0]) + getCSV(trainingRuns[1]);
        byte[] expectedResult = (header + expectedString).getBytes(StandardCharsets.UTF_8);
        // since the buffer will be 0-initialized, we create another similar-sized buffer for easy comparison
        byte[] expected = new byte[1024];
        System.arraycopy(expectedResult, 0, expected, 0, expectedResult.length);
        byte[] buffer = new byte[1024];

        try (ByteArrayInputStream bais = new ByteArrayInputStream(exportedFile.getContent())) {
            assertEquals("training_instance-id" + exportTrainingInstance.getId() + "-scores", exportedFile.getTitle());
            bais.read(buffer);
            assertArrayEquals(expected, buffer);
        } catch (IOException ex) {
            fail();
        }
    }

    private void deepEqualsTrainingDefinitionDTO(TrainingDefinitionByIdDTO t1, TrainingDefinitionByIdDTO t2) {
        assertEquals(t1.getId(), t2.getId());
        assertEquals(t1.getState(), t2.getState());
        assertEquals(t1.getDescription(), t2.getDescription());
        assertEquals(t1.getTitle(), t2.getTitle());
        assertEquals(t1.getBetaTestingGroupId(), t2.getBetaTestingGroupId());
        assertEquals(t1.getLevels(), t2.getLevels());
    }

    private String getCSV(TrainingRun trainingRun) {
        return trainingRun.getTrainingInstance().getId() + DELIMITER
                + userService.getUserRefDTOByUserRefId(trainingRun.getParticipantRef().getUserRefId()).getUserRefSub() + DELIMITER
                + trainingRun.getTotalTrainingScore() + System.lineSeparator();
    }
}

