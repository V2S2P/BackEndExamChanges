package app.entities;

import app.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String slug; //unique identifier for external API.

    @Enumerated(EnumType.STRING)
    private Category category;
    private String description;

    @ManyToMany(mappedBy = "skills")
    private List<Candidate> candidates = new ArrayList<>();

    public void addCandidate(Candidate candidate) {
        if (candidate == null) return;
        if (!candidates.contains(candidate)) {
            candidates.add(candidate);
        }
        if (!candidate.getSkills().contains(this)) {
            candidate.getSkills().add(this);
        }
    }
}
