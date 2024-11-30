package cz.muni.ics.kypo.training.persistence.model.question;

import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.persistence.model.enums.QuestionType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import lombok.*;

@EqualsAndHashCode
@Getter
@Setter
@ToString
@Entity
@Table(name = "question")
public class Question implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questionGenerator")
    @SequenceGenerator(name = "questionGenerator", sequenceName = "question_seq")
    @Column(name = "question_id", nullable = false, unique = true)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;
    @Column(name = "text")
    private String text;
    @Column(name = "order_in_assessment")
    private int order;
    @Column(name = "points")
    private int points;
    @Column(name = "penalty")
    private int penalty;
    @Column(name = "answer_required")
    private boolean answerRequired;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_level_id")
    private AssessmentLevel assessmentLevel;
    @OrderBy("order asc")
    @OneToMany(
            mappedBy = "question",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<QuestionChoice> choices = new ArrayList<>();
    @OrderBy("order asc")
    @OneToMany(
            mappedBy = "question",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ExtendedMatchingStatement> extendedMatchingStatements = new ArrayList<>();
    @OrderBy("order asc")
    @OneToMany(
            mappedBy = "question",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ExtendedMatchingOption> extendedMatchingOptions = new ArrayList<>();

    public void setChoices(List<QuestionChoice> questionChoices) {
        this.choices = questionChoices;
        this.choices.forEach(choice -> choice.setQuestion(this));
    }

    public List<ExtendedMatchingStatement> getExtendedMatchingStatements() {
        return extendedMatchingStatements;
    }

    public void setExtendedMatchingStatements(List<ExtendedMatchingStatement> extendedMatchingStatements) {
        this.extendedMatchingStatements = extendedMatchingStatements;
        this.extendedMatchingStatements.forEach(statement -> statement.setQuestion(this));
    }

    public List<ExtendedMatchingOption> getExtendedMatchingOptions() {
        return extendedMatchingOptions;
    }

    public void setExtendedMatchingOptions(List<ExtendedMatchingOption> extendedMatchingOptions) {
        this.extendedMatchingOptions = extendedMatchingOptions;
        this.extendedMatchingOptions.forEach(option -> option.setQuestion(this));
    }
}
