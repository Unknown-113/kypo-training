package cz.muni.ics.kypo.training.facade;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.HintClusterable;
import cz.muni.ics.kypo.training.api.dto.visualization.clusteranalysis.WrongAnswersClusterable;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.service.ClusterAnalysisService;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import org.apache.commons.math3.stat.clustering.Cluster;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClusterAnalysisFacade {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ElasticsearchApiService elasticsearchApiService;
    private final ClusterAnalysisService clusterAnalysisService;
    private final TrainingInstanceService trainingInstanceService;

    public ClusterAnalysisFacade(ElasticsearchApiService elasticsearchApiService,
                                 ClusterAnalysisService clusterAnalysisService,
                                 TrainingInstanceService trainingInstanceService) {
        this.elasticsearchApiService = elasticsearchApiService;
        this.clusterAnalysisService = clusterAnalysisService;
        this.trainingInstanceService = trainingInstanceService;
    }

    /**
     *  Retrieve a list of {@link WrongAnswersClusterable}s from the specified sources
     *
     * @param definitionId id of training definition
     * @param instanceIds list of instance ids
     * @param numberOfClusters number of clusters
     * @param levelId id of level, can be null
     * @return list of {@link WrongAnswersClusterable}s
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Cluster<WrongAnswersClusterable>> getWrongAnswersCluster(Long definitionId, List<Long> instanceIds, int numberOfClusters, Long levelId) {
        List<WrongAnswersClusterable> features = clusterAnalysisService
                .transformToWrongAnswersClusterable(this.loadTrainingEvents(definitionId, instanceIds, levelId));
        return clusterAnalysisService.findClusters(features, numberOfClusters);
    }

    /**
     * Retrieve sum of squared errors for wrong answer clusters
     *
     * @param definitionId id of training definition
     * @param instanceIds list of instance ids
     * @param maxNumberOfClusters max number of clusters
     * @param levelId id of level, can be null
     * @return list of values
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Double> getWrongAnswersSSE(Long definitionId, List<Long> instanceIds, int maxNumberOfClusters, Long levelId) {
        List<WrongAnswersClusterable> features = clusterAnalysisService
                .transformToWrongAnswersClusterable(this.loadTrainingEvents(definitionId, instanceIds, levelId));
        return clusterAnalysisService.calculateSSE(features, maxNumberOfClusters);
    }

    /**
     *  Retrieve a list of {@link HintClusterable}s from the specified sources
     *
     * @param definitionId id of training definition
     * @param instanceIds list of instance ids
     * @param numberOfClusters number of clusters
     * @param levelId id of level, can be null
     * @return list of {@link HintClusterable}s
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Cluster<HintClusterable>> getHintCluster(Long definitionId, List<Long> instanceIds, int numberOfClusters, Long levelId) {
        List<HintClusterable> features = clusterAnalysisService
                .transformToHintClusterable(this.loadTrainingEvents(definitionId, instanceIds, levelId));
        return clusterAnalysisService.findClusters(features, numberOfClusters);
    }

    /**
     * Retrieve sum of squared errors for hint clusters
     *
     * @param definitionId id of training definition
     * @param instanceIds list of instance ids
     * @param maxNumberOfClusters max number of clusters
     * @param levelId id of level, can be null
     * @return list of values
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Double> getHintClusterSSE(Long definitionId, List<Long> instanceIds, int maxNumberOfClusters, Long levelId) {
        List<HintClusterable> features = clusterAnalysisService
                .transformToHintClusterable(this.loadTrainingEvents(definitionId, instanceIds, levelId));
        return clusterAnalysisService.calculateSSE(features, maxNumberOfClusters);
    }

    /**
     *  Retrieve a list of {@link EuclideanDoublePoint}s from the specified sources
     *
     * @param definitionId id of training definition
     * @param instanceIds list of instance ids
     * @param numberOfClusters number of clusters
     * @param levelId id of level, can be null
     * @return list of {@link EuclideanDoublePoint}s
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Cluster<EuclideanDoublePoint>> getNDimensionalCluster(Long definitionId, List<Long> instanceIds, int numberOfClusters, Long levelId) {
        List<EuclideanDoublePoint> features = clusterAnalysisService
                .transformToNDimensionalCluster(this.loadTrainingEvents(definitionId, instanceIds, levelId));
        return clusterAnalysisService.findClusters(features, numberOfClusters);
    }

    /**
     * Retrieve sum of squared errors for n-dimensional clusters
     *
     * @param definitionId id of training definition
     * @param instanceIds list of instance ids
     * @param maxNumberOfClusters max number of clusters
     * @param levelId id of level, can be null
     * @return list of values
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public List<Double> getNDimensionalSSE(Long definitionId, List<Long> instanceIds, int maxNumberOfClusters, Long levelId) {
        List<EuclideanDoublePoint> features = clusterAnalysisService
                .transformToNDimensionalCluster(this.loadTrainingEvents(definitionId, instanceIds, levelId));
        return clusterAnalysisService.calculateSSE(features, maxNumberOfClusters);
    }

    private List<AbstractAuditPOJO> loadTrainingEvents(Long definitionId, List<Long> instanceIds, Long levelId) {
        List<AbstractAuditPOJO> events;
        if (!CollectionUtils.isEmpty(instanceIds)) {
            List<TrainingInstance> trainingInstances = trainingInstanceService.findAllByIds(instanceIds);
            checkForInstancesOfDifferentDefinition(trainingInstances, definitionId);
            events = trainingInstances.stream()
                    .flatMap(ti -> elasticsearchApiService.findAllEventsFromTrainingInstance(ti).stream())
                    .collect(Collectors.toList());
        } else {
            events = elasticsearchApiService.findAllEventsFromTrainingDefinition(definitionId);
        }
        return levelId == null ? events : filterTrainingEvents(events, levelId);
    }

    private List<AbstractAuditPOJO> filterTrainingEvents(List<AbstractAuditPOJO> events, Long levelId) {
        return events.stream().filter((event) -> event.getLevel() == levelId).collect(Collectors.toList());
    }

    private void checkForInstancesOfDifferentDefinition(List<TrainingInstance> trainingInstances, Long trainingDefinitionId) {
        Optional<TrainingInstance> trainingInstance = trainingInstances.stream()
                .filter(ti -> !ti.getTrainingDefinition().getId().equals(trainingDefinitionId))
                .findFirst();
        if (trainingInstance.isPresent()) {
            throw new EntityConflictException(new EntityErrorDetail(
                    TrainingInstance.class,
                    "id",
                    trainingInstance.get().getId().getClass(),
                    trainingInstance.get().getId(),
                    "Training instance has not assigned the correct training definition (ID: " + trainingDefinitionId + ")." )
            );
        }
    }


}
