package cz.muni.ics.kypo.training.facade.visualization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.CommandDTO;
import cz.muni.ics.kypo.training.api.enums.MistakeType;
import cz.muni.ics.kypo.training.exceptions.BadRequestException;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.mapping.mapstruct.TrainingRunMapper;
import cz.muni.ics.kypo.training.persistence.model.QTrainingRun;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.service.*;
import cz.muni.ics.kypo.training.service.api.ElasticsearchApiService;
import cz.muni.ics.kypo.training.service.api.TrainingFeedbackApiService;
import cz.muni.ics.kypo.training.utils.AbstractCommandPrefixes;
import cz.muni.ics.kypo.training.utils.ElasticSearchCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@Transactional
public class CommandVisualizationFacade {

    private static final Logger LOG = LoggerFactory.getLogger(CommandVisualizationFacade.class);

    private final TrainingInstanceService trainingInstanceService;
    private final TrainingRunService trainingRunService;
    private final SecurityService securityService;
    private final UserService userService;
    private final TrainingFeedbackApiService trainingFeedbackApiService;
    private final TrainingRunMapper trainingRunMapper;
    private final ElasticsearchApiService elasticsearchApiService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CommandVisualizationFacade(TrainingInstanceService trainingInstanceService,
                                      TrainingRunService trainingRunService,
                                      SecurityService securityService,
                                      UserService userService,
                                      TrainingFeedbackApiService trainingFeedbackApiService,
                                      TrainingRunMapper trainingRunMapper, ElasticsearchApiService elasticsearchApiService, ObjectMapper objectMapper) {
        this.trainingInstanceService = trainingInstanceService;
        this.trainingRunService = trainingRunService;
        this.securityService = securityService;
        this.userService = userService;
        this.trainingFeedbackApiService = trainingFeedbackApiService;
        this.trainingRunMapper = trainingRunMapper;
        this.elasticsearchApiService = elasticsearchApiService;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_DESIGNER) " +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public Object getReferenceGraphByDefinitionId(Long definitionId) {
        return trainingFeedbackApiService.getReferenceGraph(definitionId);
    }
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_DESIGNER) " +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public Object getReferenceGraphByInstanceId(Long instanceId) {
        TrainingInstance trainingInstance = this.trainingInstanceService.findById(instanceId);
        return trainingFeedbackApiService.getReferenceGraph(trainingInstance.getTrainingDefinition().getId());
    }

    @PreAuthorize("@securityService.isTraineeOfGivenTrainingRun(#runId)")
    public Object getReferenceGraphByRunId(Long runId) {
        TrainingRun trainingRun = trainingRunService.findById(runId);
        if(trainingRun.getState() != TRState.FINISHED) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId, "Training run has not been finished yet." ));
        }
        return trainingFeedbackApiService.getReferenceGraph(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
    }

    @IsOrganizerOrAdmin
    public Object getSummaryGraph(Long instanceId) {
        return trainingFeedbackApiService.getSummaryGraph(instanceId);
    }

    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR, " +
            "T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER) " +
            "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
    public Object getTraineeGraph(Long runId) {
        TrainingRun trainingRun = trainingRunService.findById(runId);
        if(trainingRun.getState() != TRState.FINISHED) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId, "Training run has not been finished yet." ));
        }
        return trainingFeedbackApiService.getTraineeGraph(runId);
    }

    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)")
    public Object getAggregatedCommandsForOrganizer(Long instanceId, List<Long> runIds, boolean correct, List<MistakeType> mistakeTypes) {
        if(correct) {
            this.checkIfTrainingRunsAreFinished(runIds);
            return trainingFeedbackApiService.getAggregatedCorrectCommands(runIds);
        } else {
            if(mistakeTypes == null || mistakeTypes.isEmpty()) {
                throw new BadRequestException("You must specify at least one mistake type.");
            }
            this.checkIfTrainingRunsAreFinished(runIds);
            return trainingFeedbackApiService.getAggregatedIncorrectCommands(runIds, mistakeTypes);
        }
    }


    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
    public Object getAggregatedCommandsForTrainee(Long runId, boolean correct, List<MistakeType> mistakeTypes) {
        if(correct) {
            this.checkIfTrainingRunIsFinished(runId);
            return trainingFeedbackApiService.getAggregatedCorrectCommands(List.of(runId));
        } else {
            if(mistakeTypes == null || mistakeTypes.isEmpty()) {
                throw new BadRequestException("You must specify at least one mistake type.");
            }
            this.checkIfTrainingRunIsFinished(runId);
            return trainingFeedbackApiService.getAggregatedIncorrectCommands(List.of(runId), mistakeTypes);
        }
    }

    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) " +
            "or @securityService.isOrganizerOfGivenTrainingRun(#runId) " +
            "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
    public Object getAllCommandsByFinishedTrainingRun(Long runId) {
        this.checkIfTrainingRunIsFinished(runId);
        return trainingFeedbackApiService.getAllCommandsByTrainingRun(runId);
    }

    @IsOrganizerOrAdmin
    @TransactionalRO
    public List<TrainingRunDTO> findTrainingRunsByTrainingInstance(Long instanceId) {
        Page<TrainingRun> trainingRuns = trainingInstanceService.findFinishedTrainingRunsByTrainingInstance(instanceId, PageRequest.of(0, 999));
        List<TrainingRunDTO> trainingRunDTOs = trainingRunMapper.mapToListDTO(trainingRuns.getContent());
        addParticipantsToTrainingRunDTOs(trainingRunDTOs);
        return trainingRunDTOs;
    }


    private void addParticipantsToTrainingRunDTOs(List<TrainingRunDTO> trainingRunDTOS) {
        trainingRunDTOS.forEach(trainingRunDTO ->
                trainingRunDTO.setParticipantRef(userService.getUserRefDTOByUserRefId(trainingRunDTO.getParticipantRef().getUserRefId())));
    }


    private void checkIfTrainingRunIsFinished(Long runId) {
        TrainingRun trainingRun = trainingRunService.findById(runId);
        if(trainingRun.getState() != TRState.FINISHED) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", runId.getClass(), runId, "Training run has not been finished yet." ));
        }
    }

    private void checkIfTrainingRunsAreFinished(List<Long> runIds) {
        QTrainingRun trainingRun = QTrainingRun.trainingRun;
        BooleanExpression queryRunningTrainingRuns = trainingRun.id.in(runIds).and(trainingRun.state.eq(TRState.RUNNING));
        Page<TrainingRun> runningTrainingRuns = trainingRunService.findAll(queryRunningTrainingRuns, PageRequest.of(0, 1));
        if(!runningTrainingRuns.isEmpty()) {
            throw new EntityConflictException(new EntityErrorDetail(TrainingRun.class, "id", runningTrainingRuns.getContent().get(0).getId().getClass(),
                    runningTrainingRuns.getContent().get(0).getId(), "Training run has not been finished yet." ));
        }
    }

    /**
     * Retrieve all commands from a specific Training Instance
     * @param trainingInstanceId id of Training Instance
     * @return map of {@link TrainingRunDTO} and commands from a specific {@link TrainingInstance}
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) " +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public Map<Long, List<CommandDTO>> getAllCommandsByTrainingInstance(Long trainingInstanceId) {
        Set<TrainingRun> trainingRunSet = trainingRunService.findAllByTrainingInstanceId(trainingInstanceId);
        Map<Long, List<CommandDTO>> commandsByTrainingRun = new HashMap<>(trainingRunSet.size());
        trainingRunSet.stream().map(TrainingRun::getId).forEach(runId -> commandsByTrainingRun.put(runId, getAllCommandsByTrainingRun(runId)));
        return commandsByTrainingRun;
    }

    /**
     * Retrieve all commands from a specific training run
     * @param runId id of the Training Run
     * @return list of commands executed during a training run
     */
    @PreAuthorize("hasAnyAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR) " +
            "or @securityService.isOrganizerOfGivenTrainingRun(#runId)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#runId)")
    @TransactionalRO
    public List<CommandDTO> getAllCommandsByTrainingRun(Long runId) {
        TrainingRun trainingRun = trainingRunService.findById(runId);
        TrainingInstance trainingInstance = trainingRun.getTrainingInstance();
        List<Map<String, Object>> elasticCommands;

        if (trainingInstance.isLocalEnvironment()) {
            String accessToken = trainingInstance.getAccessToken();
            Long userId = trainingRun.getParticipantRef().getId();
            elasticCommands = elasticsearchApiService.findAllConsoleCommandsByAccessTokenAndUserId(accessToken, userId);
        } else {
            String sandboxId = trainingRun.getSandboxInstanceRefId() == null ? trainingRun.getPreviousSandboxInstanceRefId() : trainingRun.getSandboxInstanceRefId();
            elasticCommands = elasticsearchApiService.findAllConsoleCommandsBySandbox(sandboxId);
        }

        return elasticCommandsToCommandDTOlist(elasticCommands, trainingRun.getStartTime());
    }

    private List<CommandDTO> elasticCommandsToCommandDTOlist(List<Map<String, Object>> elasticCommands, LocalDateTime runStartTime) {
        List<CommandDTO> commandDTOS = new ArrayList<>(elasticCommands.size());
        elasticCommands.stream()
                .map(elasticCommand -> objectMapper.convertValue(elasticCommand, ElasticSearchCommand.class))
                .forEach(command -> commandDTOS.add(elasticSearchCommandToCommandDTO(command, runStartTime)));
        return commandDTOS;
    }

    private CommandDTO elasticSearchCommandToCommandDTO(ElasticSearchCommand elasticSearchCommand, LocalDateTime runStartTime) {
        String[] commandSplit =  elasticSearchCommand.getCmd().split(" ", 2);
        String command = commandSplit[0];
        if (AbstractCommandPrefixes.isPrefix(command)) {
            commandSplit = commandSplit[1].split(" ", 2);
            command += " " + commandSplit[0];
        }

        // if there were no options, the option string should be null
        String options = null;
        if (commandSplit.length == 2) {
            options = commandSplit[1];
        }
        String timestampString = elasticSearchCommand.getTimestampStr();
        LocalDateTime commandTimestamp = ZonedDateTime.parse(timestampString).toLocalDateTime();

        return CommandDTO.builder()
                .commandType(elasticSearchCommand.getCmdType())
                .cmd(command)
                .timestamp(commandTimestamp)
                .trainingTime(Duration.between(runStartTime, commandTimestamp))
                .fromHostIp(elasticSearchCommand.getIp())
                .options(options)
                .build();
    }
}
