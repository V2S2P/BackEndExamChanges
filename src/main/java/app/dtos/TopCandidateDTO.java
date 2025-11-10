package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TopCandidateDTO {
    private Integer candidateId;
    private double averagePopularityScore;
}
