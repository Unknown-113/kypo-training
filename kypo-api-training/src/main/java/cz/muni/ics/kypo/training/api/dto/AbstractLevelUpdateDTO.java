package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.muni.ics.kypo.training.api.dto.accesslevel.AccessLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.*;

/**
 * Encapsulates information about abstract level.
 * Extended by {@link AssessmentLevelDTO}, {@link TrainingLevelDTO}, {@link AccessLevelUpdateDTO} and {@link InfoLevelDTO}
 *
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "AbstractLevelUpdateDTO", subTypes = {TrainingLevelUpdateDTO.class, AccessLevelUpdateDTO.class, AssessmentLevelUpdateDTO.class, InfoLevelUpdateDTO.class},
        description = "Superclass for classes TrainingLevelUpdateDTO, AccessLevelUpdateDTO, AssessmentLevelUpdateDTO and InfoLevelUpdateDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "level_type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TrainingLevelUpdateDTO.class, name = "TRAINING_LEVEL"),
        @JsonSubTypes.Type(value = TrainingLevelUpdateDTO.class, name = "GAME_LEVEL"),
        @JsonSubTypes.Type(value = AccessLevelUpdateDTO.class, name = "ACCESS_LEVEL"),
        @JsonSubTypes.Type(value = AssessmentLevelUpdateDTO.class, name = "ASSESSMENT_LEVEL"),
        @JsonSubTypes.Type(value = InfoLevelUpdateDTO.class, name = "INFO_LEVEL")})
public abstract class AbstractLevelUpdateDTO {

    @ApiModelProperty(value = "Main identifier of level.", required = true, example = "1")
    @NotNull(message = "{abstractLevel.id.NotNull.message}")
    protected Long id;
    @ApiModelProperty(value = "Short textual description of the level.", required = true, example = "Training Level1")
    @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
    protected String title;
    @ApiModelProperty(value = "Type of the level.", example = "TRAINING_LEVEL")
    protected LevelType levelType;
}

