package app.mappers;

import app.dtos.SkillDTO;
import app.entities.Skill;

import java.util.List;
import java.util.stream.Collectors;

public class SkillMapper {

    public static SkillDTO toDTO(Skill skill) {
        if (skill == null) return null;

        SkillDTO dto = new SkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setCategory(skill.getCategory());
        dto.setDescription(skill.getDescription());
        return dto;
    }

    public static Skill toEntity(SkillDTO dto) {
        if (dto == null) return null;

        Skill skill = new Skill();
        skill.setId(dto.getId());
        skill.setName(dto.getName());
        skill.setCategory(dto.getCategory());
        skill.setDescription(dto.getDescription());
        // candidates not set here — handled in service layer
        return skill;
    }

    public static List<SkillDTO> toDTOList(List<Skill> skills) {
        return skills.stream()
                .map(SkillMapper::toDTO)
                .collect(Collectors.toList());
    }
}
