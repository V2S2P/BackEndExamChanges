package app.dtos;

import app.enums.Category;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillDTO {
    private Integer id;
    private String name;
    private Category category;
    private String description;

    //Added variables for external API
    private Integer popularityScore;
    private Integer averageSalary;

    private String slug; //Needed to match external api

}
