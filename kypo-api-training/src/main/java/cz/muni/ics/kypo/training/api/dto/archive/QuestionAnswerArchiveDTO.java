package cz.muni.ics.kypo.training.api.dto.archive;

import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class QuestionAnswerArchiveDTO {

    private String question;
    private Set<String> answer = new HashSet<>();
}
