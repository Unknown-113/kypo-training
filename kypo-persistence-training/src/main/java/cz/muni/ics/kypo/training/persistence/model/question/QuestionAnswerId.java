package cz.muni.ics.kypo.training.persistence.model.question;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class QuestionAnswerId implements Serializable {

    @Column(name = "question_id")
    private Long questionId;
    @Column(name = "training_run_id")
    private Long trainingRunId;
}