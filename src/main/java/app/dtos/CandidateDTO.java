package app.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CandidateDTO {
    private Integer id;

    private String name;
    private String phone;
    private String educationBackground;

    private List<SkillDTO> skills;
}
