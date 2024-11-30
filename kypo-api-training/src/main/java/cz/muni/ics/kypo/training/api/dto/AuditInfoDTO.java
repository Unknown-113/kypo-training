package cz.muni.ics.kypo.training.api.dto;

import lombok.*;

/**
 * Encapsulates information used for auditing.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AuditInfoDTO {

    private long userRefId;
    private long sandboxId;
    private long poolId;
    private long trainingRunId;
    private long trainingDefinitionId;
    private long trainingInstanceId;
    private long trainingTime;
    private long level;
    private int totalScore;
    private int actualScoreInLevel;
}
