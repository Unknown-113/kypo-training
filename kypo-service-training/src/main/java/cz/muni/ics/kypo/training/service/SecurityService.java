package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.enums.RoleTypeSecurity;
import cz.muni.ics.kypo.training.exceptions.*;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.UserRef;
import cz.muni.ics.kypo.training.persistence.repository.TrainingDefinitionRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingInstanceRepository;
import cz.muni.ics.kypo.training.persistence.repository.TrainingRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


/**
 * The type Security service.
 */
@Service
@TransactionalRO(propagation = Propagation.REQUIRES_NEW)
public class SecurityService {

    private final TrainingRunRepository trainingRunRepository;
    private final TrainingDefinitionRepository trainingDefinitionRepository;
    private final TrainingInstanceRepository trainingInstanceRepository;
    private final WebClient userManagementWebClient;

    /**
     * Instantiates a new Security service.
     *
     * @param trainingInstanceRepository   the training instance repository
     * @param trainingDefinitionRepository the training definition repository
     * @param trainingRunRepository        the training run repository
     * @param userManagementWebClient      the java rest template
     */
    @Autowired
    public SecurityService(TrainingInstanceRepository trainingInstanceRepository,
                           TrainingDefinitionRepository trainingDefinitionRepository,
                           TrainingRunRepository trainingRunRepository,
                           @Qualifier("userManagementServiceWebClient") WebClient userManagementWebClient) {
        this.trainingDefinitionRepository = trainingDefinitionRepository;
        this.trainingInstanceRepository = trainingInstanceRepository;
        this.trainingRunRepository = trainingRunRepository;
        this.userManagementWebClient = userManagementWebClient;
    }

    /**
     * Is trainee of given training run boolean.
     *
     * @param trainingRunId the training run id
     * @return the boolean
     */
    public boolean isTraineeOfGivenTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunRepository.findById(trainingRunId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(),
                        trainingRunId, "The necessary permissions are required for a resource.")));
        return trainingRun.getParticipantRef().getUserRefId().equals(getUserRefIdFromUserAndGroup());
    }

    /**
     * Is organizer of given training instance boolean.
     *
     * @param instanceId the instance id
     * @return the boolean
     */
    public boolean isOrganizerOfGivenTrainingInstance(Long instanceId) {
        TrainingInstance trainingInstance = trainingInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingInstance.class, "id", instanceId.getClass(),
                        instanceId, "The necessary permissions are required for a resource.")));
        return isOrganizerOfGivenInstance(trainingInstance);
    }

    /**
     * Is organizer of given training run.
     *
     * @param trainingRunId the run id
     * @return the boolean
     */
    public boolean isOrganizerOfGivenTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunRepository.findById(trainingRunId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingRun.class, "id", trainingRunId.getClass(),
                        trainingRunId, "The necessary permissions are required for a resource.")));
        return trainingRun.getTrainingInstance().getOrganizers().stream()
                .anyMatch(o -> o.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
    }

    /**
     * Is designer of given training definition boolean.
     *
     * @param definitionId the definition id
     * @return the boolean
     */
    public boolean isDesignerOfGivenTrainingDefinition(Long definitionId) {
        TrainingDefinition trainingDefinition = trainingDefinitionRepository.findById(definitionId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(TrainingDefinition.class,
                        "id", definitionId.getClass(), definitionId, "The necessary permissions are required for a resource.")));
        return trainingDefinition.getAuthors().stream()
                .anyMatch(a -> a.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
    }

    /**
     * Is organizer of one of the training instances from the given training definition
     * @param definitionId the definition id
     * @return the boolean
     */
    public boolean isOrganizerForGivenTrainingDefinition(Long definitionId) {
        List<TrainingInstance> instances = trainingInstanceRepository.findAllByTrainingDefinitionId(definitionId);
        for (TrainingInstance trainingInstance : instances) {
            if (isOrganizerOfGivenInstance(trainingInstance)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOrganizerOfGivenInstance(TrainingInstance trainingInstance) {
        return trainingInstance.getOrganizers().stream()
                .anyMatch(o -> o.getUserRefId().equals(getUserRefIdFromUserAndGroup()));
    }

    /**
     * Has role boolean.
     *
     * @param roleTypeSecurity the role type security
     * @return the boolean
     */
    public boolean hasRole(RoleTypeSecurity roleTypeSecurity) {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority gA : authentication.getAuthorities()) {
            if (gA.getAuthority().equals(roleTypeSecurity.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets user ref id from user and group.
     *
     * @return the user ref id from user and group
     */
    public Long getUserRefIdFromUserAndGroup() {
        try {
            UserRefDTO userRefDTO = userManagementWebClient
                    .get()
                    .uri("/users/info")
                    .retrieve()
                    .bodyToMono(UserRefDTO.class)
                    .block();
            return userRefDTO.getUserRefId();
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling user management service API to get info about logged in user.", ex);
        }
    }

    /**
     * Create user ref entity by info from user and group user ref.
     *
     * @return the user ref
     */
    public UserRef createUserRefEntityByInfoFromUserAndGroup() {
        try {
            UserRefDTO userRefDTO = userManagementWebClient
                    .get()
                    .uri("/users/info")
                    .retrieve()
                    .bodyToMono(UserRefDTO.class)
                    .block();
            UserRef userRef = new UserRef();
            userRef.setUserRefId(userRefDTO.getUserRefId());
            return userRef;
        } catch (CustomWebClientException ex) {
            throw new MicroserviceApiException("Error when calling user management service API to get info about logged in user.", ex);
        }
    }

    public String getBearerToken() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return authentication.getToken().getTokenValue();
    }
}
