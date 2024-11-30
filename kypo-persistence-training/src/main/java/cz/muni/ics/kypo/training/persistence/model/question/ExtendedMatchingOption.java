package cz.muni.ics.kypo.training.persistence.model.question;

import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "extend_matching_option")
public class ExtendedMatchingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "extendedMatchingOptionGenerator")
    @SequenceGenerator(name = "extendedMatchingOptionGenerator", sequenceName = "extend_matching_option_seq")
    @Column(name = "extend_matching_option_id", nullable = false, unique = true)
    private Long id;
    @Column(name = "text")
    private String text;
    @Column(name = "order_in_row")
    private int order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
