package cz.muni.ics.kypo.training.facade;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.analytical.LevelAnalyticalDashboardDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.analytical.ParticipantAnalyticalDashboardDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.analytical.ParticipantLevelAnalyticalDashboardDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.analytical.TrainingInstanceAnalyticalDashboardDTO;
import cz.muni.ics.kypo.training.facade.visualization.VisualizationFacade;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.service.*;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnalyticalDashboardFacade {

    private final TrainingDefinitionService trainingDefinitionService;
    private final TrainingInstanceService trainingInstanceService;
    private final VisualizationFacade visualizationFacade;
    private final ElasticsearchApiService elasticsearchApiService;

    public AnalyticalDashboardFacade(TrainingDefinitionService trainingDefinitionService,
                                      TrainingInstanceService trainingInstanceService,
                                      VisualizationFacade visualizationFacade,
                                      ElasticsearchApiService elasticsearchApiService) {
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingInstanceService = trainingInstanceService;
        this.visualizationFacade = visualizationFacade;
        this.elasticsearchApiService = elasticsearchApiService;
    }

    /**
     * Gather all the necessary information for designer to display analytical dashboard.
     *
     * @param definitionId id of training definition.
     * @return data for analytical dashboard
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)" +
            "or @securityService.isOrganizerForGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<TrainingInstanceAnalyticalDashboardDTO> getDataForAnalyticalDashboard(Long definitionId) {
        List<TrainingLevel> trainingLevels = trainingDefinitionService.findAllLevelsFromDefinition(definitionId).stream()
                .filter(level -> level instanceof TrainingLevel)
                .map(level -> (TrainingLevel) level)
                .toList();
        Set<Long> trainingLevelIds = trainingLevels.stream()
                .map(AbstractLevel::getId)
                .collect(Collectors.toSet());

        List<TrainingInstance> trainingInstances = trainingDefinitionService.findAllTrainingInstancesByTrainingDefinitionId(definitionId);
        List<TrainingInstanceAnalyticalDashboardDTO> result = new ArrayList<>();

        //INSTANCES
        for (TrainingInstance instance: trainingInstances) {
            TrainingInstanceData instanceData = new TrainingInstanceData(instance.getId(), trainingLevels);
            var eventsByTrainingRunsAndLevels= elasticsearchApiService.getAggregatedEventsByTrainingRunsAndLevels(instance.getId());
            List<ParticipantAnalyticalDashboardDTO> participantsDetails = processParticipants(eventsByTrainingRunsAndLevels, instanceData, trainingLevelIds);

            TrainingInstanceAnalyticalDashboardDTO analysedInstance = new TrainingInstanceAnalyticalDashboardDTO();
            analysedInstance.setInstanceId(instance.getId());
            analysedInstance.setTitle(instance.getTitle());
            analysedInstance.setDate(instance.getStartTime().toLocalDate());
            analysedInstance.setDuration(Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis());
            analysedInstance.setLevels(new ArrayList<>(instanceData.analysedLevelById.values()));
            analysedInstance.getLevels().sort(Comparator.comparingInt(LevelAnalyticalDashboardDTO::getLevelOrder));
            analysedInstance.setParticipants(participantsDetails);
            analysedInstance.setAverageScore(instanceData.participantsScore.stream().reduce(0, Integer::sum).doubleValue() / participantsDetails.size());
            analysedInstance.setMedianScore(computeMedian(instanceData.participantsScore));
            result.add(analysedInstance);
        }
        return result;
    }

    private List<ParticipantAnalyticalDashboardDTO> processParticipants(Map<Long, Map<Long, List<AbstractAuditPOJO>>> eventsByTrainingRunsAndLevels,
                                                                        TrainingInstanceData instanceData,
                                                                        Set<Long> trainingLevelIds) {
        List<ParticipantAnalyticalDashboardDTO> result = new ArrayList<>();
        for (var runEventsByLevels: eventsByTrainingRunsAndLevels.entrySet()) {
            if(!instanceData.finishedTrainingRuns.contains(runEventsByLevels.getKey()) || runEventsByLevels.getValue().isEmpty()) {
                continue;
            }
            ParticipantAnalyticalDashboardDTO participantDetails = new ParticipantAnalyticalDashboardDTO();
            participantDetails.setLevels(processLevels(runEventsByLevels.getValue(), trainingLevelIds, instanceData.analysedLevelById, instanceData.participantsScore));
            participantDetails.setUserRefId(runEventsByLevels.getValue().entrySet().iterator().next().getValue().get(0).getUserRefId());
            result.add(participantDetails);
        }

        Map<Long, UserRefDTO> participantsByIds = visualizationFacade.getParticipantsForGivenTrainingInstance(instanceData.instanceId)
                .stream().collect(Collectors.toMap(UserRefDTO::getUserRefId, Function.identity()));
        for (ParticipantAnalyticalDashboardDTO participant : result) {
            participant.setUserName(participantsByIds.get(participant.getUserRefId()).getUserRefFullName());
        }
        return result;
    }

    private List<ParticipantLevelAnalyticalDashboardDTO> processLevels(Map<Long, List<AbstractAuditPOJO>> runEventsByLevels,
                                                                       Set<Long> trainingLevelIds,
                                                                       Map<Long, LevelAnalyticalDashboardDTO> analysedLevelsById,
                                                                       List<Integer> participantsScore) {
        List<ParticipantLevelAnalyticalDashboardDTO> result = new ArrayList<>();
        int trainingRunScore = 0;
        for (var eventsByLevel : runEventsByLevels.entrySet()) {
            if (!trainingLevelIds.contains(eventsByLevel.getKey())) {
                continue;
            }
            ParticipantLevelAnalyticalDashboardDTO participantLevelDetail = new ParticipantLevelAnalyticalDashboardDTO();
            for (AbstractAuditPOJO event : eventsByLevel.getValue()) {
                if (event instanceof HintTaken) {
                    participantLevelDetail.increaseHintTaken();
                } else if (event instanceof CorrectAnswerSubmitted) {
                    analysedLevelsById.get(event.getLevel()).addCorrectAnswerSubmit();
                } else if (event instanceof WrongAnswerSubmitted wrongAnswerEvent) {
                    analysedLevelsById.get(event.getLevel()).getWrongAnswers().add(wrongAnswerEvent.getAnswerContent());
                    participantLevelDetail.addWrongAnswer(wrongAnswerEvent.getAnswerContent());
                } else if (event instanceof TrainingRunEnded || event instanceof LevelCompleted) {
                    trainingRunScore = event.getTotalTrainingScore();
                }
            }
            AbstractAuditPOJO firstEvent = eventsByLevel.getValue().get(0);
            AbstractAuditPOJO lastEvent = eventsByLevel.getValue().get(eventsByLevel.getValue().size() - 1);
            participantLevelDetail.setDuration(lastEvent.getTrainingTime() - firstEvent.getTrainingTime());
            participantLevelDetail.setScore(lastEvent.getActualScoreInLevel());
            participantLevelDetail.setLevelId(firstEvent.getLevel());
            participantLevelDetail.setLevelTitle(analysedLevelsById.get(firstEvent.getLevel()).getLevelTitle());
            result.add(participantLevelDetail);
        }
        participantsScore.add(trainingRunScore);
        return result;
    }

    private Set<Long> getSetOfFinishedTrainingRuns(Long instanceId) {
        return trainingInstanceService.findFinishedTrainingRunsByTrainingInstance(instanceId, PageRequest.of(0,999))
                .getContent().stream()
                .filter(run -> run.getState() == TRState.FINISHED || run.getState() == TRState.ARCHIVED)
                .map(TrainingRun::getId)
                .collect(Collectors.toSet());
    }

    private Double computeMedian(List<Integer> array) {
        if(array.isEmpty()) {
            return null;
        } else if(array.size() == 1) {
            return array.get(0) + 0.0;
        }
        Collections.sort(array);
        if(array.size() % 2 == 0) {
            Integer middleLeft = array.get((array.size()/2)-1);
            Integer middleRight = array.get(array.size()/2);
            return (middleLeft + middleRight) / 2.0;
        } else {
            return array.get(array.size() / 2) + 0.0;
        }
    }

    private class TrainingInstanceData {
        Long instanceId;
        List<Integer> participantsScore = new ArrayList<>();
        Map<Long, LevelAnalyticalDashboardDTO> analysedLevelById;
        Set<Long> finishedTrainingRuns;

        public TrainingInstanceData(Long instanceId, List<TrainingLevel> trainingLevels) {
            this.instanceId = instanceId;
            this.analysedLevelById = trainingLevels.stream()
                    .collect(Collectors.toMap(AbstractLevel::getId, level -> new LevelAnalyticalDashboardDTO(level.getId(), level.getOrder(), level.getTitle(), level.getAnswer(), level.getAnswerVariableName())));
            this.finishedTrainingRuns = getSetOfFinishedTrainingRuns(instanceId);
        }
    }
}
