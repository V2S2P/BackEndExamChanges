package app.entities;

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
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String phone;
    private String educationBackground;

    @ManyToMany
    @JoinTable(
            name = "candidate_skill", // explicit join table name
            joinColumns = @JoinColumn(name = "candidate_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills = new ArrayList<>();

    public void addSkill(Skill skill) {
        if (skill == null) return;
        if (!skills.contains(skill)) {
            skills.add(skill);
        }
        if (!skill.getCandidates().contains(this)) {
            skill.getCandidates().add(this);
        }
    }
}
