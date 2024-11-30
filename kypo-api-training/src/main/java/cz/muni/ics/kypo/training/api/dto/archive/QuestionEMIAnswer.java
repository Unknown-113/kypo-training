package cz.muni.ics.kypo.training.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class QuestionEMIAnswer {

    @JsonProperty("statementOrder")
    private Integer statementOrder;
    @JsonProperty("optionOrder")
    private Integer optionOrder;
}
