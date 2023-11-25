package cz.muni.ics.kypo.training.facade.visualization;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.kypo.training.api.dto.visualization.CommandDTO;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapper;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.service.SecurityService;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.api.TrainingFeedbackApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = {
        TestDataFactory.class,
        ObjectMapper.class
})
class CommandVisualizationFacadeTest {
    private CommandVisualizationFacade commandVisualizationFacade;

    @Autowired
    private TestDataFactory testDataFactory;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainingInstanceService trainingInstanceService;
    @MockBean
    private TrainingRunService trainingRunService;
    @MockBean
    private SecurityService securityService;
    @MockBean
    private UserService userService;
    @MockBean
    private TrainingFeedbackApiService trainingFeedbackApiService;
    @MockBean
    private TrainingRunMapper trainingRunMapper;
    @MockBean
    private ElasticsearchApiService elasticsearchApiService;


    private List<Map<String, Object>> elasticCommands;
    private TrainingRun trainingRun;
    private TrainingInstance trainingInstance;
    private List<CommandDTO> expected;
    private Set<TrainingRun> trainingRuns;
    private Map<Long, List<CommandDTO>> expectedByInstance;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        commandVisualizationFacade = new CommandVisualizationFacade(trainingInstanceService, trainingRunService,
                securityService, userService, trainingFeedbackApiService, trainingRunMapper, elasticsearchApiService, objectMapper);

        elasticCommands = List.of(
                Map.of("hostname","attacker","ip","10.1.26.23","timestamp_str","2022-07-21T13:16:41.435559Z","sandbox_id","1",
                        "cmd","sudo ls","pool_id","1","wd","/home/user","cmd_type","bash-command","username","user"),
                Map.of("hostname","attacker","ip","10.1.26.23","timestamp_str","2022-07-21T13:16:42.276428Z","sandbox_id","1",
                        "cmd","cat","pool_id","1","wd","/home/user","cmd_type","bash-command","username","user"),
                Map.of("hostname","attacker","ip","10.1.26.23","timestamp_str","2022-07-21T13:16:52.658178Z","sandbox_id","1",
                        "cmd","echo f > a","pool_id","1","wd","/home/user","cmd_type","bash-command","username","user"),
                Map.of("hostname","attacker","ip","10.1.26.23","timestamp_str","2022-07-21T13:17:09.732708Z","sandbox_id","1","cmd",
                        "sudo nmap -v","pool_id","1","wd","/home/user","cmd_type","bash-command","username","user")
        );

        expected = List.of(
                new CommandDTO("bash-command", "sudo ls", LocalDateTime.parse("2022-07-21T13:16:41.435559"),
                        Duration.parse("PT1.435559S"), "10.1.26.23", null),
                new CommandDTO("bash-command", "cat", LocalDateTime.parse("2022-07-21T13:16:42.276428"),
                        Duration.parse("PT2.276428S"), "10.1.26.23", null),
                new CommandDTO("bash-command", "echo", LocalDateTime.parse("2022-07-21T13:16:52.658178"),
                        Duration.parse("PT12.658178S"), "10.1.26.23", "f > a"),
                new CommandDTO("bash-command", "sudo nmap", LocalDateTime.parse("2022-07-21T13:17:09.732708"),
                        Duration.parse("PT29.732708S"), "10.1.26.23", "-v")
        );

        LocalDateTime startTime = LocalDateTime.parse("2022-07-21T13:16:40");

        trainingRun = testDataFactory.getFinishedRun();
        trainingInstance = testDataFactory.getConcludedInstance();
        trainingRun.setStartTime(startTime);
        trainingRun.setTrainingInstance(trainingInstance);
        trainingInstance.setLocalEnvironment(false);

        expectedByInstance = Map.of(
                0L, List.of(expected.get(0)),
                1L, List.of(expected.get(1)),
                2L, List.of(expected.get(2)),
                3L, List.of(expected.get(3))
        );

        trainingRuns = new HashSet<>(4);
        for (long i = 0; i < 4L; i++) {
            TrainingRun run = testDataFactory.getFinishedRun();
            run.setId(i);
            run.setStartTime(startTime);
            run.setTrainingInstance(trainingInstance);
            run.setIncorrectAnswerCount((int) i);
            run.setSandboxInstanceRefId(String.valueOf(i));
            trainingRuns.add(run);
        }
    }

    @Test
    void getAllCommandsByTrainingRun() {
        given(trainingRunService.findById(anyLong())).willReturn(trainingRun);
        given(elasticsearchApiService.findAllConsoleCommandsBySandbox(anyString())).willReturn(elasticCommands);
        List<CommandDTO> received = commandVisualizationFacade.getAllCommandsByTrainingRun(anyLong());
        assertEquals(expected.size(), received.size());
        compareCommandDTOLists(expected, received);
    }

    @Test
    void trainingRunDoesNotExist() {
        given(trainingRunService.findById(anyLong())).willThrow(EntityNotFoundException.class);
        assertThrows(EntityNotFoundException.class, () -> commandVisualizationFacade.getAllCommandsByTrainingRun(anyLong()));
    }

    @Test
    void noCommandsFound() {
        given(trainingRunService.findById(anyLong())).willReturn(trainingRun);
        trainingInstance.setLocalEnvironment(false);
        given(elasticsearchApiService.findAllConsoleCommandsBySandbox(anyString())).willReturn(new ArrayList<>());
        List<CommandDTO> received = commandVisualizationFacade.getAllCommandsByTrainingRun(anyLong());
        assertEquals(0, received.size());
    }

    @Test
    void getAllCommandsByTrainingInstance() {
        given(trainingRunService.findAllByTrainingInstanceId(anyLong())).willReturn(trainingRuns);
        for (long i = 0; i < 4L; i++) {
            given(trainingRunService.findById(i)).willReturn(getTrainingRun(i));
            given(elasticsearchApiService.findAllConsoleCommandsBySandbox(String.valueOf(i))).willReturn(List.of(elasticCommands.get((int) i)));
        }
        Map<Long, List<CommandDTO>> received = commandVisualizationFacade.getAllCommandsByTrainingInstance(anyLong());

        for (Long runId : received.keySet()) {
            compareCommandDTOLists(received.get(runId), expectedByInstance.get(runId));
        }
    }

    private void compareCommandDTOLists(List<CommandDTO> list1, List<CommandDTO> list2) {
        if (list1.size() != list2.size()) {
            fail();
        }

        for (int i = 0; i < list1.size(); i++) {
            compareCommands(list1.get(i), list2.get(i));
        }
    }

    private TrainingRun getTrainingRun(long runId) {
        for (TrainingRun run : trainingRuns) {
            if (run.getId() == runId) {
                return run;
            }

        }
        return null;
    }

    private void compareCommands(CommandDTO cmd1, CommandDTO cmd2) {
        assertEquals(cmd1.getCmd(), cmd2.getCmd());
        assertEquals(cmd1.getOptions(), cmd2.getOptions());
        assertEquals(cmd1.getTimestamp(), cmd2.getTimestamp());
        assertEquals(cmd1.getTrainingTime(), cmd2.getTrainingTime());
        assertEquals(cmd1.getCommandType(), cmd2.getCommandType());
        assertEquals(cmd1.getFromHostIp(), cmd2.getFromHostIp());
    }
}