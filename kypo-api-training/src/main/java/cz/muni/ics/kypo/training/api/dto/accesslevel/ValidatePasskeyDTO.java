package cz.muni.ics.kypo.training.api.dto.accesslevel;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@ToString
public class ValidatePasskeyDTO {

    @ApiModelProperty(value = "Passkey to be validated.", required = true, example = "passkey")
    @NotEmpty(message = "{passkeyToValidate.passkey.NotEmpty.message}")
    private String passkey;
}
