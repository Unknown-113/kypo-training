package cz.muni.ics.kypo.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class SandboxAnswersInfo {

    @ApiModelProperty(value = "The identifier of a sandbox for that we store the answers", example = "12")
    @JsonProperty("sandbox_ref_id")
    private String sandboxRefId;
    @ApiModelProperty(value = "The access token of the training instance used to identify local sandbox.", example = "token-1234")
    @JsonProperty("access_token")
    private String accessToken;
    @ApiModelProperty(value = "The identifier of a user used to identify local sandbox", example = "12")
    @JsonProperty("user_id")
    private Long userId;
    @ApiModelProperty(value = "The answers for given sandbox")
    @JsonProperty("sandbox_answers")
    private List<VariantAnswer> variantAnswers;

    public String getSandboxRefId() {
        return sandboxRefId;
    }

    public void setSandboxRefId(String sandboxRefId) {
        this.sandboxRefId = sandboxRefId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<VariantAnswer> getVariantAnswers() {
        return variantAnswers;
    }

    public void setVariantAnswers(List<VariantAnswer> variantAnswers) {
        this.variantAnswers = variantAnswers;
    }

    @Override
    public String toString() {
        return "SandboxAnswersInfo{" +
                "sandboxRefId=" + sandboxRefId +
                ", accessToken='" + accessToken + '\'' +
                ", userId=" + userId +
                ", variantAnswers=" + variantAnswers +
                '}';
    }
}
