package cz.muni.ics.kypo.training.mapping.mapstruct;

import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.accesslevel.AccessLevelDTO;
import cz.muni.ics.kypo.training.api.dto.accesslevel.AccessLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.accesslevel.AccessLevelViewDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.preview.AssessmentLevelPreviewDTO;
import cz.muni.ics.kypo.training.api.dto.export.*;
import cz.muni.ics.kypo.training.api.dto.imports.AccessLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelPreviewDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelViewDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.TrainingLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.*;
import cz.muni.ics.kypo.training.api.dto.imports.InfoLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.LevelDefinitionProgressDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.exceptions.InternalServerErrorException;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

/**
 * The InfoLevelMapper is an utility class to map items into data transfer objects. It provides the implementation of mappings between Java bean type InfoLevelMapper and
 * DTOs classes. Code is generated during compile time.
 *
 */
@Mapper(componentModel = "spring", uses = {
        HintMapper.class, AttachmentMapper.class,
        QuestionMapper.class, ReferenceSolutionNodeMapper.class,
        MitreTechniqueMapper.class
},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LevelMapper extends ParentMapper {
    // INFO LEVEL
    InfoLevel mapToEntity(InfoLevelDTO dto);

    @Mapping(target = "levelType", constant = "INFO_LEVEL")
    BasicLevelInfoDTO mapTo(InfoLevel infoLevel);

    InfoLevel mapImportToEntity(InfoLevelImportDTO dto);

    InfoLevel mapUpdateToEntity(InfoLevelUpdateDTO dto);

    @Mapping(target = "levelType", constant = "INFO_LEVEL")
    InfoLevelDTO mapToInfoLevelDTO(InfoLevel entity);

    InfoLevelVisualizationDTO mapToVisualizationInfoLevelDTO(InfoLevel entity);

    InfoLevelExportDTO mapToExportInfoLevelDTO(InfoLevel entity);

    // ASSESSMENT LEVEL
    AssessmentLevel mapToEntity(AssessmentLevelDTO dto);

    @Mapping(source = "type", target = "assessmentType")
    AssessmentLevel mapUpdateToEntity(AssessmentLevelUpdateDTO dto);

    AssessmentLevel mapImportToEntity(AssessmentLevelImportDTO dto);

    @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
    BasicLevelInfoDTO mapTo(AssessmentLevel assessmentLevel);

    @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
    AssessmentLevelDTO mapToAssessmentLevelDTO(AssessmentLevel entity);

    @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
    AssessmentLevelPreviewDTO mapToAssessmentLevelPreviewDTO(AssessmentLevel entity);

    AssessmentLevelVisualizationDTO mapToVisualizationAssessmentLevelDTO(AssessmentLevel entity);

    @Mapping(source = "questions", target = "questions", qualifiedByName = "questionWithoutId")
    AssessmentLevelExportDTO mapToExportAssessmentLevelDTO(AssessmentLevel entity);

    AssessmentType mapToApiType(cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType assessmentType);

    // TRAINING LEVEL
    TrainingLevel mapToEntity(TrainingLevelDTO dto);

    @Mapping(target = "answer", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(dto.getAnswer()) ? null : dto.getAnswer())")
    @Mapping(target = "answerVariableName", expression = "java(org.apache.commons.lang3.StringUtils.isBlank(dto.getAnswerVariableName()) ? null : dto.getAnswerVariableName())")
    TrainingLevel mapUpdateToEntity(TrainingLevelUpdateDTO dto);

    TrainingLevel mapImportToEntity(TrainingLevelImportDTO dto);

    @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
    BasicLevelInfoDTO mapTo(TrainingLevel trainingLevel);

    TrainingLevelDTO mapToTrainingLevelDTO(TrainingLevel entity);

    TrainingLevelVisualizationDTO mapToVisualizationTrainingLevelDTO(TrainingLevel entity);

    @Mapping(source = "mitreTechniques", target = "mitreTechniques", qualifiedByName = "ignoreIds")
    TrainingLevelExportDTO mapToExportTrainingLevelDTO(TrainingLevel entity);

    @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
    TrainingLevelViewDTO mapToViewDTO(TrainingLevel entity);

    @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
    @Mapping(target = "hints", ignore = true)
    TrainingLevelPreviewDTO mapToPreviewDTO(TrainingLevel entity);

    default String mapExpectedCommandToString(ExpectedCommand entity) {
        return entity.getCommand();
    }

    default ExpectedCommand mapStringToExpectedCommand(String command) {
        ExpectedCommand expectedCommand = new ExpectedCommand();
        expectedCommand.setCommand(command);
        return expectedCommand;
    }

    // ACCESS LEVEL
    AccessLevel mapToEntity(AccessLevelDTO dto);

    AccessLevel mapUpdateToEntity(AccessLevelUpdateDTO dto);

    AccessLevel mapImportToEntity(AccessLevelImportDTO dto);

    @Mapping(target = "levelType", constant = "ACCESS_LEVEL")
    BasicLevelInfoDTO mapTo(AccessLevel trainingLevel);

    @Mapping(target = "levelType", constant = "ACCESS_LEVEL")
    AccessLevelDTO mapToAccessLevelDTO(AccessLevel entity);

    AccessLevelVisualizationDTO mapToVisualizationAccessLevelDTO(AccessLevel entity);

    AccessLevelExportDTO mapToExportAccessLevelDTO(AccessLevel entity);

    @Mapping(target = "levelType", constant = "ACCESS_LEVEL")
    AccessLevelViewDTO mapToViewDTO(AccessLevel entity);

    // ABSTRACT

    List<AbstractLevel> mapToLevels(List<AbstractLevelUpdateDTO> dtos);

    default AbstractLevel mapToAbstractLevel(AbstractLevelUpdateDTO dto) {
        if (dto.getLevelType() == LevelType.TRAINING_LEVEL) {
            return mapUpdateToEntity((TrainingLevelUpdateDTO) dto);
        } else if (dto.getLevelType() == LevelType.INFO_LEVEL) {
            return mapUpdateToEntity((InfoLevelUpdateDTO) dto);
        } else if (dto.getLevelType() == LevelType.ASSESSMENT_LEVEL) {
            return mapUpdateToEntity((AssessmentLevelUpdateDTO) dto);
        } else {
            throw new InternalServerErrorException("Level with id: " + dto.getId() + " and with title: " + dto.getTitle() +
                    " is not instance of assessment, training or info level.");
        }
    }

    default AbstractLevelDTO mapToDTO(AbstractLevel entity) {
        AbstractLevelDTO abstractLevelDTO;
        if (entity instanceof TrainingLevel) {
            abstractLevelDTO = mapToTrainingLevelDTO((TrainingLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.TRAINING_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelDTO = mapToInfoLevelDTO((InfoLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelDTO = mapToAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else if (entity instanceof AccessLevel) {
            abstractLevelDTO = mapToAccessLevelDTO((AccessLevel) entity);
            abstractLevelDTO.setLevelType(LevelType.ACCESS_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, training or info level.");
        }

        return abstractLevelDTO;
    }

    default AbstractLevelVisualizationDTO mapToVisualizationDTO(AbstractLevel entity) {
        AbstractLevelVisualizationDTO abstractLevelVisualizationDTO;
        if (entity instanceof TrainingLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationTrainingLevelDTO((TrainingLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.TRAINING_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationInfoLevelDTO((InfoLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else if (entity instanceof AccessLevel) {
            abstractLevelVisualizationDTO = mapToVisualizationAccessLevelDTO((AccessLevel) entity);
            abstractLevelVisualizationDTO.setLevelType(LevelType.ACCESS_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, training or info level.");
        }
        return abstractLevelVisualizationDTO;
    }

    default AbstractLevelExportDTO mapToExportDTO(AbstractLevel entity) {
        AbstractLevelExportDTO abstractLevelExportDTO;
        if (entity instanceof TrainingLevel) {
            abstractLevelExportDTO = mapToExportTrainingLevelDTO((TrainingLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.TRAINING_LEVEL);
        } else if (entity instanceof InfoLevel) {
            abstractLevelExportDTO = mapToExportInfoLevelDTO((InfoLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.INFO_LEVEL);
        } else if (entity instanceof AssessmentLevel) {
            abstractLevelExportDTO = mapToExportAssessmentLevelDTO((AssessmentLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else if (entity instanceof AccessLevel) {
            abstractLevelExportDTO = mapToExportAccessLevelDTO((AccessLevel) entity);
            abstractLevelExportDTO.setLevelType(LevelType.ACCESS_LEVEL);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, training or info level.");
        }
        return abstractLevelExportDTO;
    }

    @Mapping(target = "levelType", constant = "TRAINING_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(TrainingLevel entity);
    @Mapping(target = "levelType", constant = "ASSESSMENT_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(AssessmentLevel entity);
    @Mapping(target = "levelType", constant = "INFO_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(InfoLevel entity);
    @Mapping(target = "levelType", constant = "ACCESS_LEVEL")
    LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(AccessLevel entity);

    default LevelDefinitionProgressDTO mapToLevelDefinitionProgressDTO(AbstractLevel entity) {
        if (entity instanceof TrainingLevel) {
            return mapToLevelDefinitionProgressDTO((TrainingLevel) entity);
        } else if (entity instanceof InfoLevel) {
            return mapToLevelDefinitionProgressDTO((InfoLevel) entity);
        } else if (entity instanceof AssessmentLevel) {
            return mapToLevelDefinitionProgressDTO((AssessmentLevel) entity);
        } else if (entity instanceof AccessLevel) {
            return mapToLevelDefinitionProgressDTO((AccessLevel) entity);
        } else {
            throw new InternalServerErrorException("Level with id: " + entity.getId() + " in given training definition with id: " + entity.getTrainingDefinition().getId() +
                    " is not instance of assessment, training or info level.");
        }
    }
}
