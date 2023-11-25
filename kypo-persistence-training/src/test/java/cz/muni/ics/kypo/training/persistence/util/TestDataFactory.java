package cz.muni.ics.kypo.training.persistence.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.ExtendedMatchingStatementDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionChoiceDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.imports.*;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunByIdDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.technique.MitreTechniqueDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.muni.ics.kypo.training.api.dto.traininginstance.TrainingInstanceUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.Actions;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.responses.LockedPoolInfo;
import cz.muni.ics.kypo.training.api.responses.PoolInfoDTO;
import cz.muni.ics.kypo.training.api.responses.SandboxInfo;
import cz.muni.ics.kypo.training.api.responses.SandboxPoolInfo;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.enums.QuestionType;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingOption;
import cz.muni.ics.kypo.training.persistence.model.question.ExtendedMatchingStatement;
import cz.muni.ics.kypo.training.persistence.model.question.Question;
import cz.muni.ics.kypo.training.persistence.model.question.QuestionChoice;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TestDataFactory {

    private SimpleModule simpleModule = new SimpleModule("SimpleModule").addSerializer(new LocalDateTimeUTCSerializer());
    private ObjectMapper mapper = new ObjectMapper().registerModule( new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(simpleModule)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private AssessmentLevel test = generateAssessmentLevel("Test", 50, 10L,
            1, "List of instructions", AssessmentType.TEST);
    private AssessmentLevel questionnaire = generateAssessmentLevel("Questionnaire", 0, 15L,
            2, "List of some instructions", AssessmentType.QUESTIONNAIRE);
    private AssessmentLevelUpdateDTO assessmentLevelUpdateDTO = generateAssessmentLevelUpdateDTO("New Assessment Title",
             "New instructions", cz.muni.ics.kypo.training.api.enums.AssessmentType.QUESTIONNAIRE, 9);
    private AssessmentLevelImportDTO assessmentLevelImportDTO = generateAssessmentLevelImportDTO("Assessment level import", 15,
            "Questions import", "Instructions import", cz.muni.ics.kypo.training.api.enums.AssessmentType.TEST);
    private AssessmentLevelDTO assessmentLevelDTO = generateAssessmentLevelDTO("Assessment DTO", 100, 12, "DTO questions",
            "DTO instructions", cz.muni.ics.kypo.training.api.enums.AssessmentType.TEST);

    private Question freeFormQuestion = generateQuestion(0, 2, 5, QuestionType.FFQ, "Text of free form question");
    private Question multipleChoiceQuestion = generateQuestion(0, 2, 5, QuestionType.MCQ, "Text of free form question");
    private Question extendedMatchingItems = generateQuestion(0, 2, 5, QuestionType.EMI, "Text of free form question");

    private QuestionDTO freeFormQuestionDTO = generateQuestionDTO(0, 2, 5, cz.muni.ics.kypo.training.api.enums.QuestionType.FFQ, "Text of free form questionDTO");
    private QuestionDTO multipleChoiceQuestionDTO = generateQuestionDTO(0, 2, 5, cz.muni.ics.kypo.training.api.enums.QuestionType.MCQ, "Text of free form questionDTO");
    private QuestionDTO extendedMatchingItemsDTO = generateQuestionDTO(0, 2, 5, cz.muni.ics.kypo.training.api.enums.QuestionType.EMI, "Text of free form questionDTO");

    private TrainingLevel penalizedLevel = generateTrainingLevel("Penalized Training Level", 100, 20L,
            3, "SecretAnswer", "Content of penalized level", "Solution of penalized level",
            true, 5);
    private TrainingLevel nonPenalizedLevel = generateTrainingLevel("Non-Penalized Training Level", 50, 5L,
            4, "PublicAnswer", "Content of non-penalized level", "Solution of non-penalized level",
            false, 99);
    private TrainingLevelUpdateDTO trainingLevelUpdateDTO = generateTrainingLevelUpdateDTO("New Training Title", 99, "newAnswer",
            "New Content", "New solution", true, 66, 6);
    private TrainingLevelImportDTO trainingLevelImportDTO = generateTrainingLevelImportDTO("Training level import", 12, "ImportAnswer", "Game level import content",
            "import solution", true, 9, 100);
    private TrainingLevelDTO trainingLevelDTO = generateTrainingLevelDTO("DTO answer", "DTO training content", "DTO soulution", true, 8, "DTO training level",
            80, 25);

    private InfoLevel infoLevel1 = generateInfoLevel("Info level 1", 7L, 5, "Information");
    private InfoLevel infoLevel2 = generateInfoLevel("Info level 2", 9L, 6, "Content");
    private InfoLevelUpdateDTO infoLevelUpdateDTO = generateInfoLevelUpdateDTO("New Info Title", "New Info Content");
    private InfoLevelImportDTO infoLevelImportDTO = generateInfoLevelImportDTO("Info level import", 5, "Info level import content");
    private InfoLevelDTO infoLevelDTO = generateInfoLevelDTO("Info DTO", 3, "DTO content");

    private AccessLevel accessLevel = generateAccessLevel("Access level", 7L, 1, "Cloud content information",
            "Local content information. Command: ./start.sh ${USER_ID} ${ACCESS_TOKEN} ${CENTRAL_SYSLOG_IP}.", "start-training");


    private AbstractLevelDTO abstractLevelDTO = generateAbstractLevelDTO("AbstractLevelDTO", 8, LevelType.TRAINING_LEVEL, 8);
    private BasicLevelInfoDTO basicTrainingLevelInfoDTO = generateBasicLevelInfoDTO("Basic Training Level info", LevelType.TRAINING_LEVEL);
    private BasicLevelInfoDTO basicInfoLevelInfoDTO = generateBasicLevelInfoDTO("Basic Info Level info", LevelType.INFO_LEVEL);

    private MitreTechnique mitreTechnique1 = generateMitreTechnique("T1548.002");
    private MitreTechnique mitreTechnique2 = generateMitreTechnique("T2451.004");

    private MitreTechniqueDTO mitreTechniqueDTO1 = generateMitreTechniqueDTO("T3548.003");
    private MitreTechniqueDTO mitreTechniqueDTO2 = generateMitreTechniqueDTO("T5791.011");

    private AccessToken accessToken1 = generateAccessToken("test-0000");
    private AccessToken accessToken2 = generateAccessToken("token-9999");

    private Hint hint1 = generateHint("Hint 1", "Hint1 content", 25, 0);
    private Hint hint2 = generateHint("Hint 2", "Hint2 content", 50, 1);
    private Hint hint3 = generateHint("Hint 3", "Hint3 content", 75, 2);
    private HintDTO hintDTO = generateHintDTO("HintDTO", "Hint DTO content", 15);
    private HintImportDTO hintImportDTO = generateHintImportDTO("Hint Import", "Hint import content", 50);

    private TrainingDefinition unreleasedDefinition = generateTrainingDefinition("Unreleased definition", "Unreleased description",
            new String[]{"p1", "p2"}, new String[]{"o1", "o2"}, TDState.UNRELEASED, true,
            LocalDateTime.now(Clock.systemUTC()).minusHours(1), "John Doe", LocalDateTime.now(Clock.systemUTC()).minusHours(1));
    private TrainingDefinition releasedDefinition = generateTrainingDefinition("Released definition", "Released description",
            new String[]{"p3", "p4"}, new String[]{"o3"}, TDState.RELEASED, true,
            LocalDateTime.now(Clock.systemUTC()).minusHours(5), "John Doe", LocalDateTime.now(Clock.systemUTC()).minusHours(5));
    private TrainingDefinition archivedDefinition = generateTrainingDefinition("Archived definition", "Archived description",
            new String[]{"p5"}, new String[]{"o4", "o5", "o6"}, TDState.ARCHIVED, false,
            LocalDateTime.now(Clock.systemUTC()).minusHours(10),"Jane Doe", LocalDateTime.now(Clock.systemUTC()).minusHours(10));
    private TrainingDefinitionDTO unreleasedDefinitionDTO = generateTrainingDefinitionDTO(unreleasedDefinition);
    private TrainingDefinitionDTO releasedDefinitionDTO = generateTrainingDefinitionDTO(releasedDefinition);
    private TrainingDefinitionDTO archivedDefinitionDTO = generateTrainingDefinitionDTO(archivedDefinition);
    private TrainingDefinitionInfoDTO unreleasedDefinitionInfoDTO = generateTrainingDefinitionInfoDTO(unreleasedDefinition);
    private TrainingDefinitionInfoDTO releasedDefinitionInfoDTO = generateTrainingDefinitionInfoDTO(releasedDefinition);
    private TrainingDefinitionInfoDTO archivedDefinitionInfoDTO = generateTrainingDefinitionInfoDTO(archivedDefinition);
    private TrainingDefinitionCreateDTO trainingDefinitionCreateDTO = generateTrainingDefinitionCreateDTO("Training definition create DTO",
            "Creation of definition", new String[]{"p8", "p9"}, new String[]{"o8", "o9"}, cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED,
            true);
    private TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO = generateTrainingDefinitionUpdateDTO("Training definition updaet DTO",
            "Update of definition", new String[]{"p6", "p7"}, new String[]{"o6", "o7"}, cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED,
            false, 7L);
    private ImportTrainingDefinitionDTO importTrainingDefinitionDTO = generateImportTrainingDefinitionDTO("Imported definition", "Imported description",
            new String[]{"ip1", "ip2"}, new String[]{"io1", "io2"}, cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED, true);
    private TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = generateTrainingDefinitionByIdDTO("TDbyId", "Definition by id",  new String[]{"p8", "p9"},
            new String[]{"o8", "o9"}, cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED,false, false,
            20L, LocalDateTime.now(Clock.systemUTC()).minusHours(15), LocalDateTime.now(Clock.systemUTC()).minusHours(15));

    private TrainingInstance futureInstance = generateTrainingInstance(LocalDateTime.now(Clock.systemUTC()).plusHours(10),
            LocalDateTime.now(Clock.systemUTC()).plusHours(20), "Future Instance", 1L, "future-1111");
    private TrainingInstance ongoingInstance = generateTrainingInstance(LocalDateTime.now(Clock.systemUTC()).minusHours(10),
            LocalDateTime.now(Clock.systemUTC()).plusHours(10), "Ongoing Instance", 2L, "ongoing-2222");
    private TrainingInstance concludedInstance = generateTrainingInstance(LocalDateTime.now(Clock.systemUTC()).minusHours(20),
            LocalDateTime.now(Clock.systemUTC()).minusHours(5), "Concluded Instance", 3L, "concluded-3333");
    private TrainingInstanceCreateDTO trainingInstanceCreateDTO = generateTrainingInstanceCreateDTO(LocalDateTime.now(Clock.systemUTC()).plusHours(15),
            LocalDateTime.now(Clock.systemUTC()).plusHours(22), "Create Instance", "create");
    private TrainingInstanceUpdateDTO trainingInstanceUpdateDTO = generateTrainingInstanceUpdateDTO(LocalDateTime.now(Clock.systemUTC()).plusHours(5),
            LocalDateTime.now(Clock.systemUTC()).plusHours(7), "Update Instance", "update");
    private TrainingInstanceDTO trainingInstanceDTO = generateTrainingInstanceDTO(LocalDateTime.now(Clock.systemUTC()).plusHours(11),
            LocalDateTime.now(Clock.systemUTC()).plusHours(22), "Instance DTO", "DTO-5555", 10L);
    private TrainingInstanceArchiveDTO trainingInstanceArchiveDTO = generateTrainingInstanceArchiveDTO(LocalDateTime.now(Clock.systemUTC()).minusHours(20),
            LocalDateTime.now(Clock.systemUTC()).minusHours(10), "Archived instance", "archived-6666");

    private TrainingRun runningRun = generateTrainingRun(LocalDateTime.now(Clock.systemUTC()).minusHours(2), LocalDateTime.now(Clock.systemUTC()).plusHours(2),
            "logReference1", TRState.RUNNING, 2, true, "1L", 55, 21,
            200, false, "2L", 20);
    private TrainingRun finishedRun = generateTrainingRun(LocalDateTime.now(Clock.systemUTC()).minusHours(10), LocalDateTime.now(Clock.systemUTC()).minusHours(5),
            "logReference2", TRState.FINISHED, 4, false, "3L", 80, 40, 300, true, "4L", 0);
    private TrainingRun archivedRun = generateTrainingRun(LocalDateTime.now(Clock.systemUTC()).minusHours(20), LocalDateTime.now(Clock.systemUTC()).minusHours(10),
            "logReference3", TRState.ARCHIVED, 0, false, "5L", 500, 100, 600, true, "6L", 0);
    private TrainingRunByIdDTO trainingRunByIdDTO = generateTrainingRunByIdDTO(LocalDateTime.now(Clock.systemUTC()).minusHours(2), LocalDateTime.now(Clock.systemUTC()).plusHours(2),
            "logReference1", cz.muni.ics.kypo.training.api.enums.TRState.RUNNING, "5L");
    private TrainingRunDTO trainingRunDTO = generateTrainingRunDTO(LocalDateTime.now(Clock.systemUTC()).minusHours(9), LocalDateTime.now(Clock.systemUTC()).minusHours(5),
            "logReference1", cz.muni.ics.kypo.training.api.enums.TRState.FINISHED, "7L");
    private AccessedTrainingRunDTO accessedTrainingRunDTO = generateAccessedTrainingRunDTO("Accessed run", LocalDateTime.now(Clock.systemUTC()).minusHours(8), LocalDateTime.now(Clock.systemUTC()).minusHours(4), 5,
            6, Actions.RESUME);

    private PoolInfoDTO poolInfoDTO = generatePoolInfoDTO(1L, 1L, 5L, 10L, 5L, "sha", "revSha");
    private SandboxInfo sandboxInfo = generateSandboxInfo("1L", 1, 4);
    private SandboxPoolInfo sandboxPoolInfo = generateSandboxPoolInfo(1L, 1L, 10L, 5L);
    private LockedPoolInfo lockedPoolInfo = generateLockedPoolInfo(1L, 1L);

    private UserRefDTO userRefDTO1 = generateUserRefDTO(10L, "Michael Bolt", "Bolt", "Michael", "mail1@muni.cz", "https://oidc.muni.cz/oidc", null);
    private UserRefDTO userRefDTO2 = generateUserRefDTO(12L, "Peter Most", "Most", "Peter", "mail2@muni.cz", "https://oidc.muni.cz/oidc", null);
    private UserRefDTO userRefDTO3 = generateUserRefDTO(14L, "John Nevel", "Nevel", "John", "mail38@muni.cz", "https://oidc.muni.cz/oidc", null);
    private UserRefDTO userRefDTO4 = generateUserRefDTO(17L, "Ted Mosby", "Mosby", "Ted", "mail4@muni.cz", "https://oidc.muni.cz/oidc", null);

    private UserRef userRef1 = generateUserRef( 10L);
    private UserRef userRef2 = generateUserRef(12L);
    private UserRef userRef3 = generateUserRef(14L);
    private UserRef userRef4 = generateUserRef(17L);

    public AssessmentLevel getTest(){
        return clone(test, AssessmentLevel.class);
    }

    public AssessmentLevel getQuestionnaire(){
        return clone(questionnaire, AssessmentLevel.class);
    }

    public Question getFreeFormQuestion(){
        return clone(freeFormQuestion, Question.class);
    }
    public Question getMultipleChoiceQuestion(){
        return clone(multipleChoiceQuestion, Question.class);
    }
    public Question getExtendedMatchingStatements(){
        return clone(extendedMatchingItems, Question.class);
    }
    public List<ExtendedMatchingStatement> getExtendedMatchingStatements(int numberOfItems, String itemPrefix, Question question){
        List<ExtendedMatchingStatement> extendedMatchingStatements = new ArrayList<>();
        for (int i = 0; i < numberOfItems; i++) {
            extendedMatchingStatements.add(generateExtendedMatchingStatement(i, itemPrefix + " " + i, question));
        }
        return extendedMatchingStatements;
    }
    public List<ExtendedMatchingOption> getExtendedMatchingOptions(int numberOfOptions, String optionPrefix, Question question){
        List<ExtendedMatchingOption> extendedMatchingOptions = new ArrayList<>();
        for (int i = 0; i < numberOfOptions; i++) {
            extendedMatchingOptions.add(generateExtendedMatchingOption(i, optionPrefix + " " + i, question));
        }
        return extendedMatchingOptions;
    }
    public List<QuestionChoice> getQuestionChoices(int numberOfOptions, String choicePrefix, List<Boolean> correctness, Question question){
        List<QuestionChoice> questionChoices = new ArrayList<>();
        for (int i = 0; i < numberOfOptions; i++) {
            questionChoices.add(generateQuestionChoice(i, choicePrefix + " " + i, correctness.get(i), question));
        }
        return questionChoices;
    }

    public QuestionDTO getFreeFormQuestionDTO(){
        return clone(freeFormQuestionDTO, QuestionDTO.class);
    }
    public QuestionDTO getExtendedMatchingItemsDTO(){
        return clone(multipleChoiceQuestionDTO, QuestionDTO.class);
    }
    public QuestionDTO getMultipleChoiceQuestionDTO(){
        return clone(extendedMatchingItemsDTO, QuestionDTO.class);
    }
    public List<ExtendedMatchingStatementDTO> getExtendedMatchingItemDTOs(int numberOfItems, String itemPrefix, List<Integer> optionsMapping){
        List<ExtendedMatchingStatementDTO> extendedMatchingItems = new ArrayList<>();
        for (int i = 0; i < numberOfItems; i++) {
            extendedMatchingItems.add(generateExtendedMatchingItemDTO(i, itemPrefix + " " + i, optionsMapping.get(i)));
        }
        return extendedMatchingItems;
    }
    public List<ExtendedMatchingOptionDTO> getExtendedMatchingOptionDTOs(int numberOfOptions, String optionPrefix){
        List<ExtendedMatchingOptionDTO> extendedMatchingOptions = new ArrayList<>();
        for (int i = 0; i < numberOfOptions; i++) {
            extendedMatchingOptions.add(generateExtendedMatchingOptionDTO(i, optionPrefix + " " + i));
        }
        return extendedMatchingOptions;
    }
    public List<QuestionChoiceDTO> getQuestionChoiceDTOs(int numberOfOptions, String choicePrefix, List<Boolean> correctness){
        List<QuestionChoiceDTO> questionChoices = new ArrayList<>();
        for (int i = 0; i < numberOfOptions; i++) {
            questionChoices.add(generateQuestionChoiceDTO(i, choicePrefix + " " + i, correctness.get(i)));
        }
        return questionChoices;
    }

    public TrainingLevel getPenalizedLevel(){
        return clone(penalizedLevel, TrainingLevel.class);
    }

    public TrainingLevel getNonPenalizedLevel(){
        return clone(nonPenalizedLevel, TrainingLevel.class);
    }

    public InfoLevel getInfoLevel1(){
        return clone(infoLevel1, InfoLevel.class);
    }

    public InfoLevel getInfoLevel2(){
        return clone(infoLevel2, InfoLevel.class);
    }

    public AccessLevel getAccessLevel(){
        return clone(accessLevel, AccessLevel.class);
    }

    public MitreTechnique getMitreTechnique1(){
        return clone(mitreTechnique1, MitreTechnique.class);
    }

    public MitreTechnique getMitreTechnique2(){
        return clone(mitreTechnique2, MitreTechnique.class);
    }

    public AccessToken getAccessToken1(){
        return clone(accessToken1, AccessToken.class);
    }

    public AccessToken getAccessToken2(){
        return clone(accessToken2, AccessToken.class);
    }

    public Hint getHint1(){
        return clone(hint1, Hint.class);
    }

    public Hint getHint2(){
        return clone(hint2, Hint.class);
    }

    public Hint getHint3(){
        return clone(hint3, Hint.class);
    }

    public TrainingDefinition getUnreleasedDefinition(){
        return clone(unreleasedDefinition, TrainingDefinition.class);
    }

    public TrainingDefinition getReleasedDefinition(){
        return clone(releasedDefinition, TrainingDefinition.class);
    }

    public TrainingDefinition getArchivedDefinition() {
        return clone(archivedDefinition, TrainingDefinition.class);
    }

    public TrainingDefinitionDTO getUnreleasedDefinitionDTO(){
        return clone(unreleasedDefinitionDTO, TrainingDefinitionDTO.class);
    }

    public TrainingDefinitionDTO getReleasedDefinitionDTO(){
        return clone(releasedDefinitionDTO, TrainingDefinitionDTO.class);
    }

    public TrainingDefinitionDTO getArchivedDefinitionDTO(){
        return clone(archivedDefinitionDTO, TrainingDefinitionDTO.class);
    }

    public TrainingDefinitionInfoDTO getUnreleasedDefinitionInfoDTO(){
        return clone(unreleasedDefinitionInfoDTO, TrainingDefinitionInfoDTO.class);
    }

    public TrainingDefinitionInfoDTO getReleasedDefinitionInfoDTO(){
        return clone(releasedDefinitionInfoDTO, TrainingDefinitionInfoDTO.class);
    }

    public TrainingDefinitionInfoDTO getArchivedDefinitionInfoDTO(){
        return clone(archivedDefinitionInfoDTO, TrainingDefinitionInfoDTO.class);
    }

    public TrainingInstance getFutureInstance(){
        return clone(futureInstance, TrainingInstance.class);
    }

    public TrainingInstance getOngoingInstance(){
        return clone(ongoingInstance, TrainingInstance.class);
    }

    public TrainingInstance getConcludedInstance(){
        return clone(concludedInstance, TrainingInstance.class);
    }

    public TrainingRun getRunningRun(){
        return clone(runningRun, TrainingRun.class);
    }

    public TrainingRun getFinishedRun(){
        return clone(finishedRun, TrainingRun.class);
    }

    public TrainingRun getArchivedRun(){
        return clone(archivedRun, TrainingRun.class);
    }

    public TrainingDefinitionCreateDTO getTrainingDefinitionCreateDTO() {
        return clone(trainingDefinitionCreateDTO, TrainingDefinitionCreateDTO.class);
    }

    public TrainingDefinitionUpdateDTO getTrainingDefinitionUpdateDTO() {
        return clone(trainingDefinitionUpdateDTO, TrainingDefinitionUpdateDTO.class);
    }

    public TrainingInstanceCreateDTO getTrainingInstanceCreateDTO() {
        return clone(trainingInstanceCreateDTO, TrainingInstanceCreateDTO.class);
    }

    public TrainingInstanceUpdateDTO getTrainingInstanceUpdateDTO() {
        return clone(trainingInstanceUpdateDTO, TrainingInstanceUpdateDTO.class);
    }

    public AssessmentLevelUpdateDTO getAssessmentLevelUpdateDTO() {
        return clone(assessmentLevelUpdateDTO, AssessmentLevelUpdateDTO.class);
    }

    public TrainingRunByIdDTO getTrainingRunByIdDTO(){
        return clone(trainingRunByIdDTO, TrainingRunByIdDTO.class);
    }


    public TrainingRunDTO getTrainingRunDTO(){
        return clone(trainingRunDTO, TrainingRunDTO.class);
    }

    public TrainingLevelUpdateDTO getTrainingLevelUpdateDTO(){
        return clone(trainingLevelUpdateDTO, TrainingLevelUpdateDTO.class);
    }

    public InfoLevelUpdateDTO getInfoLevelUpdateDTO(){
        return clone(infoLevelUpdateDTO, InfoLevelUpdateDTO.class);
    }

    public InfoLevelImportDTO getInfoLevelImportDTO(){
        return clone(infoLevelImportDTO, InfoLevelImportDTO.class);
    }

    public TrainingLevelImportDTO getTrainingLevelImportDTO(){
        return clone(trainingLevelImportDTO, TrainingLevelImportDTO.class);
    }

    public AssessmentLevelImportDTO getAssessmentLevelImportDTO(){
        return clone(assessmentLevelImportDTO, AssessmentLevelImportDTO.class);
    }

    public ImportTrainingDefinitionDTO getImportTrainingDefinitionDTO(){
        return clone(importTrainingDefinitionDTO, ImportTrainingDefinitionDTO.class);
    }

    public TrainingDefinitionByIdDTO getTrainingDefinitionByIdDTO(){
        return clone(trainingDefinitionByIdDTO, TrainingDefinitionByIdDTO.class);
    }

    public AbstractLevelDTO getAbstractLevelDTO() {
        return clone(abstractLevelDTO, AbstractLevelDTO.class);
    }

    public BasicLevelInfoDTO getBasicTrainingLevelInfoDTO(){
        return clone(basicTrainingLevelInfoDTO, BasicLevelInfoDTO.class);
    }
    public BasicLevelInfoDTO getBasicInfoLevelInfoDTO(){
        return clone(basicInfoLevelInfoDTO, BasicLevelInfoDTO.class);
    }

    public TrainingInstanceDTO getTrainingInstanceDTO(){
        return clone(trainingInstanceDTO, TrainingInstanceDTO.class);
    }

    public InfoLevelDTO getInfoLevelDTO(){
        return clone(infoLevelDTO, InfoLevelDTO.class);
    }

    public TrainingLevelDTO getTrainingLevelDTO(){
        return clone(trainingLevelDTO, TrainingLevelDTO.class);
    }

    public AssessmentLevelDTO getAssessmentLevelDTO(){
        return clone(assessmentLevelDTO, AssessmentLevelDTO.class);
    }

    public AccessedTrainingRunDTO getAccessedTrainingRunDTO(){
        return clone(accessedTrainingRunDTO, AccessedTrainingRunDTO.class);
    }

    public HintDTO getHintDTO(){
        return clone(hintDTO, HintDTO.class);
    }

    public HintImportDTO getHintImportDTO(){
        return clone(hintImportDTO, HintImportDTO.class);
    }

    public TrainingInstanceArchiveDTO getTrainingInstanceArchiveDTO(){
        return clone(trainingInstanceArchiveDTO, TrainingInstanceArchiveDTO.class);
    }

    public PoolInfoDTO getPoolInfoDTO(){
        return clone(poolInfoDTO, PoolInfoDTO.class);
    }

    public SandboxInfo getSandboxInfo(){
        return clone(sandboxInfo, SandboxInfo.class);
    }

    public SandboxPoolInfo getSandboxPoolInfo(){
        return clone(sandboxPoolInfo, SandboxPoolInfo.class);
    }

    public LockedPoolInfo getLockedPoolInfo(){
        return clone(lockedPoolInfo, LockedPoolInfo.class);
    }

    public MitreTechniqueDTO getMitreTechniqueDTO1(){
        return clone(mitreTechniqueDTO1, MitreTechniqueDTO.class);
    }

    public MitreTechniqueDTO getMitreTechniqueDTO2(){
        return clone(mitreTechniqueDTO2, MitreTechniqueDTO.class);
    }

    public UserRefDTO getUserRefDTO1() { return clone(userRefDTO1, UserRefDTO.class);}
    public UserRefDTO getUserRefDTO2() { return clone(userRefDTO2, UserRefDTO.class);}
    public UserRefDTO getUserRefDTO3() { return clone(userRefDTO3, UserRefDTO.class);}
    public UserRefDTO getUserRefDTO4() { return clone(userRefDTO4, UserRefDTO.class);}


    public UserRef getUserRef1() { return clone(userRef1, UserRef.class);}
    public UserRef getUserRef2() { return clone(userRef2, UserRef.class);}
    public UserRef getUserRef3() { return clone(userRef3, UserRef.class);}
    public UserRef getUserRef4() { return clone(userRef4, UserRef.class);}

    private AssessmentLevel generateAssessmentLevel(String title, int maxScore, long estimatedDuration, int order,
                                                    String instructions, AssessmentType assessmentType){
        AssessmentLevel newAssessmentLevel = new AssessmentLevel();
        newAssessmentLevel.setTitle(title);
        newAssessmentLevel.setMaxScore(maxScore);
        newAssessmentLevel.setEstimatedDuration(estimatedDuration);
        newAssessmentLevel.setOrder(order);
        newAssessmentLevel.setInstructions(instructions);
        newAssessmentLevel.setAssessmentType(assessmentType);
        return newAssessmentLevel;
    }

    private Question generateQuestion(int order, int penalty, int points, QuestionType questionType, String text){
        Question newQuestion = new Question();
        newQuestion.setOrder(order);
        newQuestion.setPenalty(penalty);
        newQuestion.setPoints(points);
        newQuestion.setQuestionType(questionType);
        newQuestion.setText(text);
        return newQuestion;
    }

    private ExtendedMatchingOption generateExtendedMatchingOption(int optionOrder, String text, Question question){
            ExtendedMatchingOption extendedMatchingOption = new ExtendedMatchingOption();
            extendedMatchingOption.setOrder(optionOrder);
            extendedMatchingOption.setText(text);
            extendedMatchingOption.setQuestion(question);
            return extendedMatchingOption;
    }

    private ExtendedMatchingStatement generateExtendedMatchingStatement(int itemOrder, String itemText, Question question){
        ExtendedMatchingStatement extendedMatchingStatement = new ExtendedMatchingStatement();
        extendedMatchingStatement.setOrder(itemOrder);
        extendedMatchingStatement.setText(itemText);
        extendedMatchingStatement.setQuestion(question);
        return extendedMatchingStatement;
    }

    private QuestionChoice generateQuestionChoice(int choiceOrder, String choiceText, boolean isCorrect, Question question){
        QuestionChoice questionChoice = new QuestionChoice();
        questionChoice.setOrder(choiceOrder);
        questionChoice.setText(choiceText);
        questionChoice.setCorrect(isCorrect);
        questionChoice.setQuestion(question);
        return questionChoice;
    }

    private QuestionDTO generateQuestionDTO(int order, int penalty, int points, cz.muni.ics.kypo.training.api.enums.QuestionType questionType, String text){
        QuestionDTO newQuestion = new QuestionDTO();
        newQuestion.setOrder(order);
        newQuestion.setPenalty(penalty);
        newQuestion.setPoints(points);
        newQuestion.setQuestionType(questionType);
        newQuestion.setText(text);
        return newQuestion;
    }

    private ExtendedMatchingOptionDTO generateExtendedMatchingOptionDTO(int optionOrder, String text){
        ExtendedMatchingOptionDTO extendedMatchingOption = new ExtendedMatchingOptionDTO();
        extendedMatchingOption.setOrder(optionOrder);
        extendedMatchingOption.setText(text);
        return extendedMatchingOption;
    }

    private ExtendedMatchingStatementDTO generateExtendedMatchingItemDTO(int itemOrder, String text, int correctOptionOrder){
        ExtendedMatchingStatementDTO extendedMatchingItem = new ExtendedMatchingStatementDTO();
        extendedMatchingItem.setOrder(itemOrder);
        extendedMatchingItem.setText(text);
        extendedMatchingItem.setCorrectOptionOrder(correctOptionOrder);
        return extendedMatchingItem;
    }

    private QuestionChoiceDTO generateQuestionChoiceDTO(int choiceOrder, String text, boolean isCorrect){
        QuestionChoiceDTO questionChoice = new QuestionChoiceDTO();
        questionChoice.setOrder(choiceOrder);
        questionChoice.setText(text);
        questionChoice.setCorrect(isCorrect);
        return questionChoice;
    }

    private TrainingLevel generateTrainingLevel(String title, int maxScore, long estimatedDuration, int order, String answer,
                                            String content, String solution, boolean solutionPenalized, int incorrectAnswerLimit){
        TrainingLevel newTrainingLevel = new TrainingLevel();
        newTrainingLevel.setTitle(title);
        newTrainingLevel.setMaxScore(maxScore);
        newTrainingLevel.setEstimatedDuration(estimatedDuration);
        newTrainingLevel.setOrder(order);
        newTrainingLevel.setAnswer(answer);
        newTrainingLevel.setContent(content);
        newTrainingLevel.setSolution(solution);
        newTrainingLevel.setSolutionPenalized(solutionPenalized);
        newTrainingLevel.setIncorrectAnswerLimit(incorrectAnswerLimit);
        return newTrainingLevel;
    }

    private InfoLevel generateInfoLevel(String title, long estimatedDuration, int order, String content){
        InfoLevel newInfoLevel = new InfoLevel();
        newInfoLevel.setTitle(title);
        newInfoLevel.setMaxScore(0);
        newInfoLevel.setEstimatedDuration(estimatedDuration);
        newInfoLevel.setOrder(order);
        newInfoLevel.setContent(content);
        return newInfoLevel;
    }

    private AccessLevel generateAccessLevel(String title, long estimatedDuration, int order, String cloudContent,
                                            String localContent, String passkey){
        AccessLevel newAccessLevel = new AccessLevel();
        newAccessLevel.setTitle(title);
        newAccessLevel.setMaxScore(0);
        newAccessLevel.setEstimatedDuration(estimatedDuration);
        newAccessLevel.setOrder(order);
        newAccessLevel.setCloudContent(cloudContent);
        newAccessLevel.setLocalContent(localContent);
        newAccessLevel.setPasskey(passkey);
        return newAccessLevel;
    }

    private MitreTechnique generateMitreTechnique(String techniqueKey){
        MitreTechnique newMitreTechnique = new MitreTechnique();
        newMitreTechnique.setTechniqueKey(techniqueKey);
        return newMitreTechnique;
    }

    private MitreTechniqueDTO generateMitreTechniqueDTO(String techniqueKey){
        MitreTechniqueDTO mitreTechniqueDTO = new MitreTechniqueDTO();
        mitreTechniqueDTO.setTechniqueKey(techniqueKey);
        return mitreTechniqueDTO;
    }

    private AccessToken generateAccessToken(String accessToken){
        AccessToken newAccessToken = new AccessToken();
        newAccessToken.setAccessToken(accessToken);
        return newAccessToken;
    }

    private Hint generateHint(String title, String content, Integer hintPenalty, int order){
        Hint newHint = new Hint();
        newHint.setTitle(title);
        newHint.setContent(content);
        newHint.setHintPenalty(hintPenalty);
        newHint.setOrder(order);
        return newHint;
    }

    private TrainingDefinition generateTrainingDefinition(String title, String description, String[] prerequisites,
                                                          String[] outcomes, TDState state, boolean showStepperBar,
                                                          LocalDateTime lastEdited, String lastEditedBy,
                                                          LocalDateTime createdAt){
        TrainingDefinition newTrainingDefinition = new TrainingDefinition();
        newTrainingDefinition.setTitle(title);
        newTrainingDefinition.setDescription(description);
        newTrainingDefinition.setPrerequisites(prerequisites);
        newTrainingDefinition.setOutcomes(outcomes);
        newTrainingDefinition.setState(state);
        newTrainingDefinition.setShowStepperBar(showStepperBar);
        newTrainingDefinition.setLastEdited(lastEdited);
        newTrainingDefinition.setLastEditedBy(lastEditedBy);
        newTrainingDefinition.setCreatedAt(createdAt);
        return newTrainingDefinition;
    }

    private TrainingDefinitionDTO generateTrainingDefinitionDTO(TrainingDefinition trainingDefinition){
        TrainingDefinitionDTO trainingDefinitionDTO = new TrainingDefinitionDTO();
        trainingDefinitionDTO.setTitle(trainingDefinition.getTitle());
        trainingDefinitionDTO.setDescription(trainingDefinition.getDescription());
        trainingDefinitionDTO.setPrerequisites(trainingDefinition.getPrerequisites());
        trainingDefinitionDTO.setOutcomes(trainingDefinition.getOutcomes());
        trainingDefinitionDTO.setState(mapToTDState(trainingDefinition.getState()));
        trainingDefinitionDTO.setShowStepperBar(trainingDefinition.isShowStepperBar());
        trainingDefinitionDTO.setLastEdited(trainingDefinition.getLastEdited());
        trainingDefinitionDTO.setCreatedAt(trainingDefinitionDTO.getCreatedAt());
        return trainingDefinitionDTO;
    }

    private TrainingDefinitionInfoDTO generateTrainingDefinitionInfoDTO(TrainingDefinition trainingDefinition){
        TrainingDefinitionInfoDTO trainingDefinitionInfoDTO = new TrainingDefinitionInfoDTO();
        trainingDefinitionInfoDTO.setTitle(trainingDefinition.getTitle());
        trainingDefinitionInfoDTO.setState(mapToTDState(trainingDefinition.getState()));
        return trainingDefinitionInfoDTO;
    }

    private TrainingInstance generateTrainingInstance(LocalDateTime starTime, LocalDateTime endTime, String title,
                                                      Long poolId, String accessToken){
        TrainingInstance newTrainingInstance = new TrainingInstance();
        newTrainingInstance.setStartTime(starTime);
        newTrainingInstance.setEndTime(endTime);
        newTrainingInstance.setTitle(title);
        newTrainingInstance.setPoolId(poolId);
        newTrainingInstance.setAccessToken(accessToken);
        newTrainingInstance.setLastEdited(LocalDateTime.now().minusHours(5));
        newTrainingInstance.setLastEditedBy("kypo-user");
        return newTrainingInstance;
    }

    private TrainingRun generateTrainingRun(LocalDateTime startTime, LocalDateTime endTime, String eventLogReference, TRState state,
                                            int incorrectAnswerCount, boolean solutionTaken, String SBIRefId, int totalTrainingScore,
                                            int totalAssessmentScore, int maxScore, boolean levelAnswered, String previousSBIRefId, int currentPenalty){
        TrainingRun newTrainingRun = new TrainingRun();
        newTrainingRun.setStartTime(startTime);
        newTrainingRun.setEndTime(endTime);
        newTrainingRun.setEventLogReference(eventLogReference);
        newTrainingRun.setState(state);
        newTrainingRun.setIncorrectAnswerCount(incorrectAnswerCount);
//        newTrainingRun.setSolutionTaken(solutionTaken);
        newTrainingRun.setSandboxInstanceRefId(SBIRefId);
        newTrainingRun.setTotalTrainingScore(totalTrainingScore);
        newTrainingRun.setTotalAssessmentScore(totalAssessmentScore);
        newTrainingRun.setMaxLevelScore(maxScore);
        newTrainingRun.setLevelAnswered(levelAnswered);
        newTrainingRun.setPreviousSandboxInstanceRefId(previousSBIRefId);
        newTrainingRun.setCurrentPenalty(currentPenalty);
        return newTrainingRun;
    }

    private TrainingDefinitionCreateDTO generateTrainingDefinitionCreateDTO(String title, String description, String[] prerequisites,
                                                                      String[] outcomes, cz.muni.ics.kypo.training.api.enums.TDState state,
                                                                      boolean showStepperBar){
        TrainingDefinitionCreateDTO trainingDefinitionCreateDTO = new TrainingDefinitionCreateDTO();
        trainingDefinitionCreateDTO.setTitle(title);
        trainingDefinitionCreateDTO.setDescription(description);
        trainingDefinitionCreateDTO.setPrerequisites(prerequisites);
        trainingDefinitionCreateDTO.setOutcomes(outcomes);
        trainingDefinitionCreateDTO.setState(state);
        trainingDefinitionCreateDTO.setShowStepperBar(showStepperBar);
        return trainingDefinitionCreateDTO;
    }

    private TrainingDefinitionUpdateDTO generateTrainingDefinitionUpdateDTO(String title, String description, String[] prerequisites,
                                                                            String[] outcomes, cz.muni.ics.kypo.training.api.enums.TDState state,
                                                                            boolean showStepperBar, Long SDRefId){
        TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO = new TrainingDefinitionUpdateDTO();
        trainingDefinitionUpdateDTO.setTitle(title);
        trainingDefinitionUpdateDTO.setDescription(description);
        trainingDefinitionUpdateDTO.setPrerequisites(prerequisites);
        trainingDefinitionUpdateDTO.setOutcomes(outcomes);
        trainingDefinitionUpdateDTO.setState(state);
        trainingDefinitionUpdateDTO.setShowStepperBar(showStepperBar);
        return trainingDefinitionUpdateDTO;
    }

    private TrainingInstanceCreateDTO generateTrainingInstanceCreateDTO(LocalDateTime startTime, LocalDateTime endTime,
                                                                        String title, String accessToken){
        TrainingInstanceCreateDTO trainingInstanceCreateDTO = new TrainingInstanceCreateDTO();
        trainingInstanceCreateDTO.setStartTime(startTime);
        trainingInstanceCreateDTO.setEndTime(endTime);
        trainingInstanceCreateDTO.setTitle(title);
        trainingInstanceCreateDTO.setAccessToken(accessToken);
        return trainingInstanceCreateDTO;
    }

    private TrainingInstanceUpdateDTO generateTrainingInstanceUpdateDTO(LocalDateTime startTime, LocalDateTime endTime,
                                                                        String title, String accessToken){
        TrainingInstanceUpdateDTO trainingInstanceUpdateDTO = new TrainingInstanceUpdateDTO();
        trainingInstanceUpdateDTO.setStartTime(startTime);
        trainingInstanceUpdateDTO.setEndTime(endTime);
        trainingInstanceUpdateDTO.setTitle(title);
        trainingInstanceUpdateDTO.setAccessToken(accessToken);
        return trainingInstanceUpdateDTO;
    }

    private TrainingLevelUpdateDTO generateTrainingLevelUpdateDTO(String title, int maxScore, String answer, String content, String solution,
                                                              boolean solutionPenalized, int estimatedDuration, int incorrectAnswerLimit){
        TrainingLevelUpdateDTO trainingLevelUpdateDTO = new TrainingLevelUpdateDTO();
        trainingLevelUpdateDTO.setTitle(title);
        trainingLevelUpdateDTO.setMaxScore(maxScore);
        trainingLevelUpdateDTO.setAnswer(answer);
        trainingLevelUpdateDTO.setContent(content);
        trainingLevelUpdateDTO.setSolution(solution);
        trainingLevelUpdateDTO.setSolutionPenalized(solutionPenalized);
        trainingLevelUpdateDTO.setEstimatedDuration(estimatedDuration);
        trainingLevelUpdateDTO.setIncorrectAnswerLimit(incorrectAnswerLimit);
        return trainingLevelUpdateDTO;
    }

    private AssessmentLevelUpdateDTO generateAssessmentLevelUpdateDTO(String title, String instructions,
                                                                      cz.muni.ics.kypo.training.api.enums.AssessmentType type, int estimatedDuration){
        AssessmentLevelUpdateDTO assessmentLevelUpdateDTO = new AssessmentLevelUpdateDTO();
        assessmentLevelUpdateDTO.setTitle(title);
        assessmentLevelUpdateDTO.setInstructions(instructions);
        assessmentLevelUpdateDTO.setType(type);
        assessmentLevelUpdateDTO.setEstimatedDuration(estimatedDuration);
        return assessmentLevelUpdateDTO;
    }

    private InfoLevelUpdateDTO generateInfoLevelUpdateDTO(String title, String content){
        InfoLevelUpdateDTO infoLevelUpdateDTO = new InfoLevelUpdateDTO();
        infoLevelUpdateDTO.setTitle(title);
        infoLevelUpdateDTO.setContent(content);
        return infoLevelUpdateDTO;
    }

    private InfoLevelImportDTO generateInfoLevelImportDTO(String title, Integer estimatedDuration, String content){
        InfoLevelImportDTO infoLevelImportDTO = new InfoLevelImportDTO();
        infoLevelImportDTO.setTitle(title);
        infoLevelImportDTO.setContent(content);
        infoLevelImportDTO.setLevelType(LevelType.INFO_LEVEL);
        infoLevelImportDTO.setEstimatedDuration(estimatedDuration);
        return infoLevelImportDTO;
    }

    private AssessmentLevelImportDTO generateAssessmentLevelImportDTO(String title, Integer estimatedDuration, String questions, String instructions,
                                                                      cz.muni.ics.kypo.training.api.enums.AssessmentType type){
        AssessmentLevelImportDTO assessmentLevelImportDTO = new AssessmentLevelImportDTO();
        assessmentLevelImportDTO.setTitle(title);
        assessmentLevelImportDTO.setEstimatedDuration(estimatedDuration);
        assessmentLevelImportDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        assessmentLevelImportDTO.setInstructions(instructions);
        assessmentLevelImportDTO.setAssessmentType(type);
        return assessmentLevelImportDTO;
    }

    private TrainingLevelImportDTO generateTrainingLevelImportDTO(String title, Integer estimatedDuration, String answer, String content, String solution, boolean solutionPenalized,
                                                              int incorrectAnswerLimit, int maxScore){
        TrainingLevelImportDTO trainingLevelImportDTO = new TrainingLevelImportDTO();
        trainingLevelImportDTO.setTitle(title);
        trainingLevelImportDTO.setEstimatedDuration(estimatedDuration);
        trainingLevelImportDTO.setLevelType(LevelType.TRAINING_LEVEL);
        trainingLevelImportDTO.setAnswer(answer);
        trainingLevelImportDTO.setContent(content);
        trainingLevelImportDTO.setSolution(solution);
        trainingLevelImportDTO.setSolutionPenalized(solutionPenalized);
        trainingLevelImportDTO.setIncorrectAnswerLimit(incorrectAnswerLimit);
        trainingLevelImportDTO.setMaxScore(maxScore);
        return trainingLevelImportDTO;
    }

    private ImportTrainingDefinitionDTO generateImportTrainingDefinitionDTO(String title, String description, String[] prerequisites,
                                                                            String[] outcomes, cz.muni.ics.kypo.training.api.enums.TDState state,
                                                                            boolean showStepperBar){
        ImportTrainingDefinitionDTO importTrainingDefinitionDTO = new ImportTrainingDefinitionDTO();
        importTrainingDefinitionDTO.setTitle(title);
        importTrainingDefinitionDTO.setDescription(description);
        importTrainingDefinitionDTO.setPrerequisites(prerequisites);
        importTrainingDefinitionDTO.setOutcomes(outcomes);
        importTrainingDefinitionDTO.setState(state);
        importTrainingDefinitionDTO.setShowStepperBar(showStepperBar);
        return importTrainingDefinitionDTO;
    }

    private TrainingDefinitionByIdDTO generateTrainingDefinitionByIdDTO(String title, String description, String[] prerequisites,
                                                                        String[] outcomes, cz.muni.ics.kypo.training.api.enums.TDState state,
                                                                        boolean showStepperBar, boolean canBeArchived, long estimatedDuration,
                                                                        LocalDateTime lastEdited, LocalDateTime createdAt){
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = new TrainingDefinitionByIdDTO();
        trainingDefinitionByIdDTO.setTitle(title);
        trainingDefinitionByIdDTO.setDescription(description);
        trainingDefinitionByIdDTO.setPrerequisites(prerequisites);
        trainingDefinitionByIdDTO.setOutcomes(outcomes);
        trainingDefinitionByIdDTO.setState(state);
        trainingDefinitionByIdDTO.setShowStepperBar(showStepperBar);
        trainingDefinitionByIdDTO.setCanBeArchived(canBeArchived);
        trainingDefinitionByIdDTO.setEstimatedDuration(estimatedDuration);
        trainingDefinitionByIdDTO.setLastEdited(lastEdited);
        trainingDefinitionByIdDTO.setCreatedAt(createdAt);
        return trainingDefinitionByIdDTO;
    }

    private AbstractLevelDTO generateAbstractLevelDTO(String title, int maxScore, LevelType type, int estimatedDuration){
        AbstractLevelDTO abstractLevelDTO = new AbstractLevelDTO();
        abstractLevelDTO.setTitle(title);
        abstractLevelDTO.setMaxScore(maxScore);
        abstractLevelDTO.setLevelType(type);
        abstractLevelDTO.setEstimatedDuration(estimatedDuration);
        return abstractLevelDTO;
    }

    private BasicLevelInfoDTO generateBasicLevelInfoDTO(String title, LevelType levelType){
        BasicLevelInfoDTO basicLevelInfoDTO = new BasicLevelInfoDTO();
        basicLevelInfoDTO.setTitle(title);
        basicLevelInfoDTO.setLevelType(levelType);
        return basicLevelInfoDTO;
    }

    private TrainingInstanceDTO generateTrainingInstanceDTO(LocalDateTime start, LocalDateTime end, String title,
                                                            String accessToken, Long poolId){
        TrainingInstanceDTO trainingInstanceDTO = new TrainingInstanceDTO();
        trainingInstanceDTO.setStartTime(start);
        trainingInstanceDTO.setEndTime(end);
        trainingInstanceDTO.setTitle(title);
        trainingInstanceDTO.setAccessToken(accessToken);
        trainingInstanceDTO.setPoolId(poolId);
        return trainingInstanceDTO;
    }

    private TrainingRunByIdDTO generateTrainingRunByIdDTO(LocalDateTime start, LocalDateTime end, String logReference, cz.muni.ics.kypo.training.api.enums.TRState state,
                                                          String SBIId){
        TrainingRunByIdDTO trainingRunByIdDTO = new TrainingRunByIdDTO();
        trainingRunByIdDTO.setStartTime(start);
        trainingRunByIdDTO.setEndTime(end);
        trainingRunByIdDTO.setEventLogReference(logReference);
        trainingRunByIdDTO.setState(state);
        trainingRunByIdDTO.setSandboxInstanceRefId(SBIId);
        return trainingRunByIdDTO;
    }

    private TrainingRunDTO generateTrainingRunDTO(LocalDateTime start, LocalDateTime end, String logReference, cz.muni.ics.kypo.training.api.enums.TRState state,
                                                  String SBIId){
        TrainingRunDTO trainingRunDTO = new TrainingRunDTO();
        trainingRunDTO.setStartTime(start);
        trainingRunDTO.setEndTime(end);
        trainingRunDTO.setEventLogReference(logReference);
        trainingRunDTO.setState(state);
        trainingRunDTO.setSandboxInstanceRefId(SBIId);
        return trainingRunDTO;
    }

    private InfoLevelDTO generateInfoLevelDTO(String title, int estimatedDuration, String content){
        InfoLevelDTO infoLevelDTO = new InfoLevelDTO();
        infoLevelDTO.setTitle(title);
        infoLevelDTO.setMaxScore(0);
        infoLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        infoLevelDTO.setEstimatedDuration(estimatedDuration);
        infoLevelDTO.setContent(content);
        return infoLevelDTO;
    }

    private AssessmentLevelDTO generateAssessmentLevelDTO(String title, int maxScore, int estimatedDuration,
                                                          String questions, String instructions, cz.muni.ics.kypo.training.api.enums.AssessmentType assessmentType){
        AssessmentLevelDTO assessmentLevelDTO = new AssessmentLevelDTO();
        assessmentLevelDTO.setTitle(title);
        assessmentLevelDTO.setMaxScore(maxScore);
        assessmentLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        assessmentLevelDTO.setEstimatedDuration(estimatedDuration);
        assessmentLevelDTO.setInstructions(instructions);
        assessmentLevelDTO.setAssessmentType(assessmentType);
        return assessmentLevelDTO;
    }

    private TrainingLevelDTO generateTrainingLevelDTO(String answer, String content, String solution, boolean solutionPenalized, int answerLimit, String title, int maxScore, int estimatedDuration){
        TrainingLevelDTO trainingLevelDTO = new TrainingLevelDTO();
        trainingLevelDTO.setAnswer(answer);
        trainingLevelDTO.setContent(content);
        trainingLevelDTO.setSolution(solution);
        trainingLevelDTO.setSolutionPenalized(solutionPenalized);
        trainingLevelDTO.setIncorrectAnswerLimit(answerLimit);
        trainingLevelDTO.setTitle(title);
        trainingLevelDTO.setMaxScore(maxScore);
        trainingLevelDTO.setLevelType(LevelType.TRAINING_LEVEL);
        trainingLevelDTO.setEstimatedDuration(estimatedDuration);
        return trainingLevelDTO;
    }

    private AccessedTrainingRunDTO generateAccessedTrainingRunDTO(String title, LocalDateTime start, LocalDateTime end, int currentLevelOrder,
                                                                  int numberOfLevels, Actions possibleAction){
        AccessedTrainingRunDTO accessedTrainingRunDTO = new AccessedTrainingRunDTO();
        accessedTrainingRunDTO.setTitle(title);
        accessedTrainingRunDTO.setTrainingInstanceStartDate(start);
        accessedTrainingRunDTO.setTrainingInstanceEndDate(end);
        accessedTrainingRunDTO.setCurrentLevelOrder(currentLevelOrder);
        accessedTrainingRunDTO.setNumberOfLevels(numberOfLevels);
        accessedTrainingRunDTO.setPossibleAction(possibleAction);
        return accessedTrainingRunDTO;
    }

    private HintDTO generateHintDTO(String title, String content, Integer penalty){
        HintDTO hintDTO = new HintDTO();
        hintDTO.setTitle(title);
        hintDTO.setContent(content);
        hintDTO.setHintPenalty(penalty);
        return hintDTO;
    }

    private HintImportDTO generateHintImportDTO(String title, String content, Integer penalty){
        HintImportDTO hintImportDTO = new HintImportDTO();
        hintImportDTO.setTitle(title);
        hintImportDTO.setContent(content);
        hintImportDTO.setHintPenalty(penalty);
        return hintImportDTO;
    }

    private TrainingInstanceArchiveDTO generateTrainingInstanceArchiveDTO(LocalDateTime start, LocalDateTime end, String title, String accessToken){
        TrainingInstanceArchiveDTO trainingInstanceArchiveDTO = new TrainingInstanceArchiveDTO();
        trainingInstanceArchiveDTO.setStartTime(start);
        trainingInstanceArchiveDTO.setEndTime(end);
        trainingInstanceArchiveDTO.setTitle(title);
        trainingInstanceArchiveDTO.setAccessToken(accessToken);
        return trainingInstanceArchiveDTO;
    }

    private PoolInfoDTO generatePoolInfoDTO(Long id, Long definitionId, Long lockId, Long maxSize, Long size, String sha, String revSha){
        PoolInfoDTO poolInfoDTO = new PoolInfoDTO();
        poolInfoDTO.setId(id);
        poolInfoDTO.setDefinitionId(definitionId);
        poolInfoDTO.setLockId(lockId);
        poolInfoDTO.setMaxSize(maxSize);
        poolInfoDTO.setSize(size);
        poolInfoDTO.setRev(sha);
        poolInfoDTO.setRevSha(revSha);
        return poolInfoDTO;
    }

    private SandboxInfo generateSandboxInfo(String id, Integer lockId, Integer allocationUnit){
        SandboxInfo sandboxInfo = new SandboxInfo();
        sandboxInfo.setId(id);
        sandboxInfo.setAllocationUnitId(allocationUnit);
        sandboxInfo.setLockId(lockId);
        return sandboxInfo;
    }

    private SandboxPoolInfo generateSandboxPoolInfo(Long id, Long definitionId, Long maxSize, Long size){
        SandboxPoolInfo sandboxPoolInfo = new SandboxPoolInfo();
        sandboxPoolInfo.setId(id);
        sandboxPoolInfo.setDefinitionId(definitionId);
        sandboxPoolInfo.setMaxSize(maxSize);
        sandboxPoolInfo.setSize(size);
        return sandboxPoolInfo;
    }

    private LockedPoolInfo generateLockedPoolInfo(Long id, Long poolId){
        LockedPoolInfo lockedPoolInfo = new LockedPoolInfo();
        lockedPoolInfo.setId(id);
        lockedPoolInfo.setPoolId(poolId);
        return lockedPoolInfo;
    }

    private UserRef generateUserRef(Long userRefId) {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userRefId);
        return userRef;
    }

    private UserRefDTO generateUserRefDTO(Long userRefId, String fullName, String familyName, String givenName, String sub, String iss, byte[] picture) {
        UserRefDTO userRefDTO = new UserRefDTO();
        userRefDTO.setUserRefId(userRefId);
        userRefDTO.setUserRefFullName(fullName);
        userRefDTO.setUserRefFamilyName(familyName);
        userRefDTO.setUserRefGivenName(givenName);
        userRefDTO.setUserRefSub(sub);
        userRefDTO.setIss(iss);
        userRefDTO.setPicture(picture);
        return userRefDTO;
    }

    private <T> T clone(Object object, Class<T> tClass){
        try {
            mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
            String json = mapper.writeValueAsString(object);
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private cz.muni.ics.kypo.training.api.enums.TDState mapToTDState(TDState state) {
        switch (state) {
            case UNRELEASED: return cz.muni.ics.kypo.training.api.enums.TDState.UNRELEASED;
            case RELEASED: return cz.muni.ics.kypo.training.api.enums.TDState.RELEASED;
            case ARCHIVED: return cz.muni.ics.kypo.training.api.enums.TDState.ARCHIVED;
            case PRIVATED: return cz.muni.ics.kypo.training.api.enums.TDState.PRIVATED;
        }
        return null;
    }
}
