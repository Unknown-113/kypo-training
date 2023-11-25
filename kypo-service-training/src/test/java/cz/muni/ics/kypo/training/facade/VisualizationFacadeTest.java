package cz.muni.ics.kypo.training.facade;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.ClusteringLevelDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.ClusteringVisualizationDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.TrainingResultsDTO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.facade.visualization.VisualizationFacade;
import cz.muni.ics.kypo.training.mapping.mapstruct.LevelMapper;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.util.TestDataFactory;
import cz.muni.ics.kypo.training.service.*;
import cz.muni.ics.kypo.training.service.api.AnswersStorageApiService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = {
        TestDataFactory.class,
})
class VisualizationFacadeTest {
    @Autowired
    TestDataFactory testDataFactory;

    @MockBean
    private TrainingDefinitionService trainingDefinitionService;
    @MockBean
    private TrainingInstanceService trainingInstanceService;
    @MockBean
    private TrainingRunService trainingRunService;
    @MockBean
    private VisualizationService visualizationService;
    @MockBean
    private AnswersStorageApiService answersStorageApiService;
    @MockBean
    private ElasticsearchApiService elasticsearchApiService;
    @MockBean
    private UserService userService;
    @MockBean
    private LevelMapper levelMapper;

    private VisualizationFacade visualizationFacade;
    private TrainingDefinition trainingDefinition;
    private List<AbstractLevel> levels;
    private List<TrainingInstance> trainingInstances;
    private Map<Long, Map<Long, Map<Long, List<AbstractAuditPOJO>>>> eventsByInstanceId;
    private Map<Long, Set<TrainingRun>> runsByTrainingInstance;
    private List<UserRef> users;
    private List<Long> userIds;
    private List<UserRefDTO> userDTOs;
    private PageResultResource<UserRefDTO> resultResource1;
    private PageResultResource<UserRefDTO> resultResource2;
    private PageResultResource<UserRefDTO> resultResource3;

    private final String PICTURE = "iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAACIUlEQVR4Xu3YsY0dSQxAQQUlpXT5Z3CS/YgxSrQa4gLlEOBb9pj/x6//fv7/t/78/XhN3yBWyz3kBX2DWC33kBf0DWK13ENe0DeI1XIPeUHfIFbLPeQFfYNYLfeQF/QNYrXcQ17QN4jVcg95Qd8gVss95AV9g1gt95AX9A1itdxDXtA3iNVyD3lB3yBWyz3kBX2DWC33kBf0DWLERGOiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS6z+8b/mPha4jwXuY4H7WOA+FriPBe5jgftY4D4WuI8F7mOB+1jgPha4jwXGbzbn2xicb2Nwvo3B+TYG59sYnG9jcL6Nwfk2BufbGJxvY3C+jcH5Ngbn2xicb2Nwvq1+z2pMtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3RlvgNt34wfeJElG8AAAAASUVORK5CYII=";

    @BeforeEach
    public void init() {
        visualizationFacade = new VisualizationFacade(
                trainingDefinitionService,
                trainingInstanceService,
                trainingRunService,
                visualizationService,
                answersStorageApiService,
                elasticsearchApiService,
                userService,
                levelMapper);

        trainingDefinition = testDataFactory.getReleasedDefinition();
        trainingDefinition.setId(1L);
        trainingDefinition.setEstimatedDuration(100L);

        levels = List.of(
                testDataFactory.getPenalizedLevel(),
                testDataFactory.getNonPenalizedLevel(),
                testDataFactory.getNonPenalizedLevel(),
                testDataFactory.getTest()
        );

        for (long i = 0; i < levels.size(); i++) {
            levels.get((int) i).setId(i);
        }

        users = new ArrayList<>(9);
        userIds = new ArrayList<>(9);
        userDTOs = new ArrayList<>(9);
        byte[] decodedPicture = Base64.getDecoder().decode(PICTURE);
        for (long i = 0; i < 9; i++) {
            UserRef user = testDataFactory.getUserRef1();
            UserRefDTO userRefDTO = testDataFactory.getUserRefDTO1();
            user.setUserRefId(i);
            userRefDTO.setUserRefId(i);
            userRefDTO.setPicture(decodedPicture);
            users.add(user);
            userIds.add(i);
            userDTOs.add(userRefDTO);
        }

        resultResource1 = new PageResultResource<>(List.of(userDTOs.get(0), userDTOs.get(1), userDTOs.get(2)),
                new PageResultResource.Pagination(0, 3, 3, 3L, 1));
        resultResource2 = new PageResultResource<>(List.of(userDTOs.get(3), userDTOs.get(4), userDTOs.get(5)),
                new PageResultResource.Pagination(0, 3, 3, 3L, 1));
        resultResource3 = new PageResultResource<>(List.of(userDTOs.get(6), userDTOs.get(7), userDTOs.get(8)),
                new PageResultResource.Pagination(0, 3, 3, 3L, 1));

        trainingInstances = List.of(
                testDataFactory.getConcludedInstance(),
                testDataFactory.getConcludedInstance(),
                testDataFactory.getConcludedInstance()
        );

        runsByTrainingInstance = new HashMap<>();
        for (long instanceId = 0; instanceId < trainingInstances.size(); instanceId++) {
            TrainingInstance trainingInstance = trainingInstances.get((int) instanceId);
            trainingInstance.setId(instanceId);
            Set<TrainingRun> trainingRuns = new HashSet<>();
            for (long runId = 0; runId < 3; runId++) {
                TrainingRun trainingRun = testDataFactory.getFinishedRun();
                long id = instanceId * 3 + runId;
                trainingRun.setId(id);
                trainingRun.setParticipantRef(users.get((int) id));
                trainingRuns.add(trainingRun);
            }
            runsByTrainingInstance.put(instanceId, trainingRuns);
        }

        long trainingTime = 10L;
        int score = 10;
        // map<instanceId, map<levelId, map<runId, list<events>>>>
        eventsByInstanceId = new HashMap<>(trainingInstances.size());
        for (long instanceId = 0; instanceId < trainingInstances.size(); instanceId++) {

            Map<Long, Map<Long, List<AbstractAuditPOJO>>> instanceEvents = new HashMap<>();
            for (long levelId = 0; levelId < levels.size(); levelId++) {

                Map<Long, List<AbstractAuditPOJO>> runEvents = new HashMap<>();
                for (long runId = 0; runId < 3; runId++) {
                    runEvents.put(instanceId * 3 + runId, List.of(
                            getLevelStarted(trainingTime),
                            getCorrectAnswerSubmitted(),
                            getLevelCompleted(),
                            getTrainingRunEnded(trainingTime + 1L, score, score, score / 2))
                    );
                    trainingTime += 2;
                    score += 5;
                }
                instanceEvents.put(levelId, runEvents);

            }
            eventsByInstanceId.put(instanceId, instanceEvents);

        }

    }

    @Test
    void getClusteringVisualizationsForTrainingDefinition() {
        given(trainingDefinitionService.findById(1L)).willReturn(trainingDefinition);
        given(trainingDefinitionService.findAllLevelsFromDefinition(anyLong())).willReturn(levels);
        given(trainingDefinitionService.findAllTrainingInstancesByTrainingDefinitionId(anyLong())).willReturn(trainingInstances);

        for (TrainingInstance trainingInstance : trainingInstances) {
            given(elasticsearchApiService.getAggregatedEventsByLevelsAndTrainingRuns(trainingInstance.getId())).willReturn(eventsByInstanceId.get(trainingInstance.getId()));
            given(trainingRunService.findAllByTrainingInstanceId(trainingInstance.getId())).willReturn(runsByTrainingInstance.get(trainingInstance.getId()));
        }

        given(userService.getUsersRefDTOByGivenUserIds(List.of(0L, 1L, 2L), PageRequest.of(0, 999), null, null)).willReturn(resultResource1);
        given(userService.getUsersRefDTOByGivenUserIds(List.of(3L, 4L, 5L), PageRequest.of(0, 999), null, null)).willReturn(resultResource2);
        given(userService.getUsersRefDTOByGivenUserIds(List.of(6L, 7L, 8L), PageRequest.of(0, 999), null, null)).willReturn(resultResource3);

        ClusteringVisualizationDTO clusteringVisualizationDTO = visualizationFacade.getClusteringVisualizationsForTrainingDefinition(1L);
        TrainingResultsDTO resultsDTO = clusteringVisualizationDTO.getFinalResults();
        assertEquals(250, resultsDTO.getMaxAchievableScore());
        assertEquals(277, resultsDTO.getMaxParticipantScore());
        assertEquals(185, resultsDTO.getMaxParticipantTrainingScore());
        assertEquals(92, resultsDTO.getMaxParticipantAssessmentScore());
        assertEquals(81, resultsDTO.getMaxParticipantTime());
        assertEquals(46, resultsDTO.getAverageTime());
        assertEquals(146, resultsDTO.getAverageScore());
        assertEquals(97.5, resultsDTO.getAverageTrainingScore());
        assertEquals(48.5, resultsDTO.getAverageAssessmentScore());
        assertEquals(9, resultsDTO.getPlayerData().size());

        checkClusteringLevelDTO(clusteringVisualizationDTO.getLevels().get(0), 100, 140, 1, 1, 75);
        checkClusteringLevelDTO(clusteringVisualizationDTO.getLevels().get(2), 50, 170, 1, 1, 105);
    }

    private LevelStarted getLevelStarted(Long time) {
        LevelStarted.LevelStartedBuilder<?, ?> levelStarted = LevelStarted.builder();
        levelStarted.trainingTime(time);
        return levelStarted.build();
    }

    private LevelCompleted getLevelCompleted() {
        return new LevelCompleted();
    }

    private CorrectAnswerSubmitted getCorrectAnswerSubmitted() {
        return new CorrectAnswerSubmitted();
    }

    private TrainingRunEnded getTrainingRunEnded(Long time, int scoreInLevel, int totalTrainingScore, int totalAssessmentScore) {
        TrainingRunEnded.TrainingRunEndedBuilder<?, ?> runEnded = TrainingRunEnded.builder();
        runEnded.trainingTime(time);
        runEnded.actualScoreInLevel(scoreInLevel);
        runEnded.totalTrainingScore(totalTrainingScore);
        runEnded.totalAssessmentScore(totalAssessmentScore);
        return runEnded.build();
    }

    private void checkClusteringLevelDTO(ClusteringLevelDTO level, int maxAchievableScore, int maxParticipantScore, int maxTime, float averageTime, float averageScore) {
        assertEquals(maxAchievableScore, level.getMaxAchievableScore());
        assertEquals(maxParticipantScore, level.getMaxParticipantScore());
        assertEquals(maxTime, level.getMaxParticipantTime());
        assertEquals(averageTime, level.getAverageTime());
        assertEquals(averageScore, level.getAverageScore());
    }
}