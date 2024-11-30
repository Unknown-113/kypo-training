package cz.muni.ics.kypo.training.persistence.model;


import cz.muni.ics.kypo.training.persistence.model.enums.AssessmentType;
import cz.muni.ics.kypo.training.persistence.model.question.Question;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.*;

/**
 * Class specifying Abstract level as assessment level.
 * Assessment levels contain questions for trainees to answer.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assessment_level")
@PrimaryKeyJoinColumn(name = "id")
public class AssessmentLevel extends AbstractLevel {

    @OrderBy("order asc")
    @OneToMany(
            mappedBy = "assessmentLevel",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Question> questions = new ArrayList<>();
    @Column(name = "instructions", nullable = false)
    private String instructions;
    @Column(name = "assessment_type", length = 128, nullable = false)
    @Enumerated(EnumType.STRING)
    private AssessmentType assessmentType;

    /**
     * Sets questions that trainee must answer
     *
     * @param questions the questions
     */
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        this.questions.forEach(question -> question.setAssessmentLevel(this));
        this.questions.sort(Comparator.comparingInt(Question::getOrder));
    }
}
