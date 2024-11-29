package cz.muni.ics.kypo.training.api.dto.cheatingdetection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@ApiModel(value = "LocationSimilarityDetectionEventDTO", description = "A detection event of type Location Similarity.", parent = AbstractDetectionEventDTO.class)
public class LocationSimilarityDetectionEventDTO extends AbstractDetectionEventDTO {

    @ApiModelProperty(value = "Ip address of participant.", example = "1.1.1.1")
    private String ipAddress;
    @ApiModelProperty(value = "DNS of participant.", example = "fi.muni.cz")
    private String dns;
    @ApiModelProperty(value = "If the address is the same as deployment.", example = "false")
    private boolean isAddressDeploy;
}
