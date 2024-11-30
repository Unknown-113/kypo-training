package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.*;

/**
 * Encapsulates information about user reference.
 *
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "UserRefDTO", description = "User information from user-and-group microservice is mapped to this class " +
        "and is also used to provide information about authors, participants, and organizers.")
public class UserRefDTO {

    @JsonAlias({"id", "user_ref_id"})
    @ApiModelProperty(value = "Reference to user in another microservice and get his id", example = "1")
    private Long userRefId;
    @ApiModelProperty(value = "Reference to user in another microservice.", example = "999999@mail.muni.cz")
    @JsonProperty("sub")
    private String userRefSub;
    @ApiModelProperty(value = "Reference to user in another microservice and get his full name", example = "Mgr. John Doe")
    @JsonProperty("full_name")
    private String userRefFullName;
    @ApiModelProperty(value = "User given name", example = "John")
    @JsonProperty("given_name")
    private String userRefGivenName;
    @ApiModelProperty(value = "User family name", example = "Doe")
    @JsonProperty("family_name")
    private String userRefFamilyName;
    @ApiModelProperty(value = "Reference to user in another microservice and get his iss", example = "https://oidc.muni.cz")
    private String iss;
    @ApiModelProperty(value = "Identicon of a user.", example = "iVBORw0KGgoAAAANSUhEUgAAAEsAAABLCAYAAAA4TnrqAAACIUlEQVR4Xu3YsY0dSQxAQQUlpXT5Z3CS/YgxSrQa4gLlEOBb9pj/x6//fv7/t/78/XhN3yBWyz3kBX2DWC33kBf0DWK13ENe0DeI1XIPeUHfIFbLPeQFfYNYLfeQF/QNYrXcQ17QN4jVcg95Qd8gVss95AV9g1gt95AX9A1itdxDXtA3iNVyD3lB3yBWyz3kBX2DWC33kBf0DWLERGOiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS4yB6CGiLdGWaEuMgeghoi3RlmhLjIHoIaIt0ZZoS6z+8b/mPha4jwXuY4H7WOA+FriPBe5jgftY4D4WuI8F7mOB+1jgPha4jwXGbzbn2xicb2Nwvo3B+TYG59sYnG9jcL6Nwfk2BufbGJxvY3C+jcH5Ngbn2xicb2Nwvq1+z2pMtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3Rllgt9xDRlmhLtCVWyz1EtCXaEm2J1XIPEW2JtkRbYrXcQ0Rboi3RlvgNt34wfeJElG8AAAAASUVORK5CYII=")
    private byte[] picture;
    @ApiModelProperty(value = "Email of the user.", example = "johndoe@mail.muni.cz")
    private String mail;
}
