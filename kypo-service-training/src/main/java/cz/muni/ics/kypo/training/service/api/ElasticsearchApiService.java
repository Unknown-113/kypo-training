package cz.muni.ics.kypo.training.service.api;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.ics.kypo.training.exceptions.CustomWebClientException;
import cz.muni.ics.kypo.training.exceptions.MicroserviceApiException;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type User service.
 */
@Service
public class ElasticsearchApiService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchApiService.class);
    private static final String USER_ID_FIELD_NAME = "user_ref_id";
    private WebClient elasticsearchServiceWebClient;

    /**
     * Instantiates a new ElasticSearchApi service.
     *
     * @param elasticsearchServiceWebClient the web client
     */
    public ElasticsearchApiService(@Qualifier("elasticsearchServiceWebClient") WebClient elasticsearchServiceWebClient) {
        this.elasticsearchServiceWebClient = elasticsearchServiceWebClient;
    }

    /**
     * Deletes events from elasticsearch for particular training instance
     *
     * @param trainingInstanceId id of the training instance whose events to delete.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public void deleteEventsByTrainingInstanceId(Long trainingInstanceId){
        try {
            elasticsearchServiceWebClient
                    .delete()
                    .uri("/training-platform-events/training-instances/{instanceId}", trainingInstanceId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API to delete events for particular instance (ID: "+ trainingInstanceId +").", ex);
        }
    }

    /**
     * Obtain events from elasticsearch for particular training definition
     *
     * @param trainingDefinitionId the training definition id whose events to obtain.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public List<AbstractAuditPOJO> findAllEventsFromTrainingDefinition(Long trainingDefinitionId){
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-events/training-definitions/{definitionId}", trainingDefinitionId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AbstractAuditPOJO>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular definition (ID: "+ trainingDefinitionId +").", ex);
        }
    }

    /**
     * Obtain events from elasticsearch for particular training instance
     *
     * @param trainingInstance thee training instance whose events to obtain.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public List<AbstractAuditPOJO> findAllEventsFromTrainingInstance(TrainingInstance trainingInstance){
        try {
            Long definitionId = trainingInstance.getTrainingDefinition().getId();
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-events/training-definitions/{definitionId}/training-instances/{instanceId}", definitionId, trainingInstance.getId())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AbstractAuditPOJO>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular instance (ID: "+ trainingInstance.getId() +").", ex);
        }
    }


    /**
     * Obtain events from elasticsearch for particular training run
     *
     * @param trainingRun thee training run whose events to obtain.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public List<AbstractAuditPOJO> findAllEventsFromTrainingRun(TrainingRun trainingRun){
        try {
            Long definitionId = trainingRun.getTrainingInstance().getTrainingDefinition().getId();
            Long instanceId = trainingRun.getTrainingInstance().getId();
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-events/training-definitions/{definitionId}/training-instances/{instanceId}/training-runs/{runId}", definitionId, instanceId, trainingRun.getId())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AbstractAuditPOJO>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular run (ID: "+ trainingRun.getId() +").", ex);
        }
    }

    /**
     * Obtain events from elasticsearch for particular training instance.
     *
     * @param instanceId the training instance Id whose events to obtain.
     * @return Aggregated events by user and levels.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public Map<Long, Map<Long, List<AbstractAuditPOJO>>> getAggregatedEventsByLevelsAndTrainingRuns(Long instanceId) {
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-events/training-instances/{instanceId}/aggregated/levels/training-runs", instanceId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Long, Map<Long, List<AbstractAuditPOJO>>>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular instance (ID: "+ instanceId +").", ex);
        }
    }

    /**
     * Obtain events from elasticsearch for particular training instance.
     *
     * @param instanceId the training instance Id whose events to obtain.
     * @return Aggregated events by user and levels.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public Map<Long, Map<Long, List<AbstractAuditPOJO>>> getAggregatedEventsByTrainingRunsAndLevels(Long instanceId) {
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-events/training-instances/{instanceId}/aggregated/training-runs/levels", instanceId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Long, Map<Long, List<AbstractAuditPOJO>>>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular instance (ID: "+ instanceId +").", ex);
        }
    }

    /**
     * Obtain events from elasticsearch for particular training instance and level aggregated by users.
     *
     * @param instanceId the training instance whose events to obtain.
     * @param levelId the id of specific level whose events to obtain.
     * @return Aggregated level events by user
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public Map<Long, List<AbstractAuditPOJO>> getEventsOfTrainingInstanceAndLevelAggregatedByUsers(Long instanceId, Long levelId) {
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/training-platform-events/training-instances/{instanceId}/levels/{levelId}")
                            .queryParam("aggregationField", USER_ID_FIELD_NAME)
                            .build(instanceId, levelId)
                    )
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<Long, List<AbstractAuditPOJO>>>() {})
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular instance (ID: "+ instanceId +").", ex);
        }
    }


    /**
     * Deletes events from elasticsearch for particular training run
     *
     * @param trainingInstanceId id of the training instance in which the training run is running.
     * @param trainingRunId id of the training run whose events to delete.
     * @throws MicroserviceApiException error with specific message when calling elasticsearch microservice.
     */
    public void deleteEventsFromTrainingRun(Long trainingInstanceId, Long trainingRunId){
        try {
            elasticsearchServiceWebClient
                    .delete()
                    .uri("/training-platform-events/training-instances/{instanceId}/training-runs/{runId}", trainingInstanceId, trainingRunId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex){
            throw new MicroserviceApiException("Error when calling Elasticsearch API to delete events for particular training run (ID: "+ trainingRunId +").", ex);
        }
    }

    public List<Map<String, Object>> findAllConsoleCommandsBySandbox(String sandboxId){
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-commands/sandboxes/{sandboxId}", sandboxId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular sandbox (ID: " + sandboxId +").", ex);
        }
    }

    public List<Map<String, Object>> findAllConsoleCommandsByAccessTokenAndUserId(String accessToken, Long userId){
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri("/training-platform-commands/access-tokens/{accessToken}/users/{userId}", accessToken, userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular training " +
                    "(access-token: " + accessToken +")" + "(user: " + userId +").", ex);
        }
    }

    public List<Map<String, Object>> findAllConsoleCommandsBySandboxAndTimeRange(String sandboxId, Long from, Long to, List<String> filterCommands){
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/training-platform-commands/sandboxes/{sandboxId}/ranges")
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .queryParam("filterCommands", filterCommands)
                            .build(sandboxId)
                    )
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular commands of sandbox (ID: " + sandboxId +").", ex);
        }
    }

    public List<Map<String, Object>> findAllConsoleCommandsBySandboxAndTimeRange(String sandboxId, Long from, Long to){
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/training-platform-commands/sandboxes/{sandboxId}/ranges")
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .build(sandboxId)
                    )
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular commands of sandbox (ID: " + sandboxId +").", ex);
        }
    }

    public List<Map<String, Object>> findAllConsoleCommandsByAccessTokenAndUserIdAndTimeRange(String accessToken, Long userId, Long from, Long to){
        try {
            return elasticsearchServiceWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/training-platform-commands/access-token/{accessToken}/users/{userId}/ranges")
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .build(accessToken, userId)
                    )
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Elasticsearch API for particular commands of training " +
                    "(access-token: " + accessToken +")" + "(user: " + userId +").", ex);
        }
    }

    public void deleteCommandsByPool(Long poolId){
        try{
            elasticsearchServiceWebClient
                    .delete()
                    .uri("/training-platform-commands/pools/{poolId}", poolId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Elasticsearch API to delete bash commands for particular pool (ID: " + poolId +").", ex);
        }
    }

    public void deleteCommandsByAccessToken(String accessToken){
        try{
            elasticsearchServiceWebClient
                    .delete()
                    .uri("/training-platform-commands/access-tokens/{accessToken}", accessToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Elasticsearch API to delete bash commands for particular training instance (access-token: " + accessToken +").", ex);
        }
    }

    public void deleteCommandsBySandbox(String sandboxId){
        try{
            elasticsearchServiceWebClient
                    .delete()
                    .uri("/training-platform-commands/sandboxes/{sandboxId}", sandboxId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Elasticsearch API to delete bash commands for particular sandbox (ID: " + sandboxId +").", ex);
        }
    }

    public void deleteCommandsByAccessTokenAndUserId(String accessToken, Long userId){
        try{
            elasticsearchServiceWebClient
                    .delete()
                    .uri("/training-platform-commands/access-tokens/{accessToken}/users/{userId}", accessToken, userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling Elasticsearch API to delete bash commands for particular training (Access Token: " + accessToken +", User ID: " + userId +").", ex);
        }
    }
}

