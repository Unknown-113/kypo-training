package cz.muni.ics.kypo.training.persistence.model.question;

import javax.persistence.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "question_choice")
public class QuestionChoice implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questionChoiceGenerator")
    @SequenceGenerator(name = "questionChoiceGenerator", sequenceName = "question_choice_seq")
    @Column(name = "question_choice_id", nullable = false, unique = true)
    private Long id;
    @Column(name = "text")
    private String text;
    @Column(name = "correct")
    private boolean correct;
    @Column(name = "order_in_question")
    private int order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
