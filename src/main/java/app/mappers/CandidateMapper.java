package app.mappers;

import app.dtos.CandidateDTO;
import app.dtos.SkillDTO;
import app.entities.Candidate;
import app.entities.Skill;

import java.util.List;
import java.util.stream.Collectors;

public class CandidateMapper {

    public static CandidateDTO toDTO(Candidate candidate) {
        if (candidate == null) return null;

        CandidateDTO dto = new CandidateDTO();
        dto.setId(candidate.getId());
        dto.setName(candidate.getName());
        dto.setPhone(candidate.getPhone());
        dto.setEducationBackground(candidate.getEducationBackground());

        // Convert each skill to a SkillDTO
        if (candidate.getSkills() != null) {
            dto.setSkills(candidate.getSkills()
                    .stream()
                    .map(CandidateMapper::mapSkillToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
    public static CandidateDTO toDTO(Candidate candidate, boolean includeSkills) {
        if (candidate == null) return null;

        CandidateDTO dto = new CandidateDTO();
        dto.setId(candidate.getId());
        dto.setName(candidate.getName());
        dto.setPhone(candidate.getPhone());
        dto.setEducationBackground(candidate.getEducationBackground());

        // Convert each skill to a SkillDTO
        if (includeSkills && candidate.getSkills() != null) {
            dto.setSkills(candidate.getSkills()
                    .stream()
                    .map(CandidateMapper::mapSkillToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static Candidate toEntity(CandidateDTO dto) {
        if (dto == null) return null;

        Candidate candidate = new Candidate();
        candidate.setId(dto.getId());
        candidate.setName(dto.getName());
        candidate.setPhone(dto.getPhone());
        candidate.setEducationBackground(dto.getEducationBackground());

        // Skills will be set by service layer if needed (to avoid detached entities)
        return candidate;
    }

    private static SkillDTO mapSkillToDTO(Skill skill) {
        SkillDTO dto = new SkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setCategory(skill.getCategory());
        dto.setDescription(skill.getDescription());
        dto.setSlug(skill.getSlug());
        return dto;
    }

    public static List<CandidateDTO> toDTOList(List<Candidate> candidates) {
        return candidates.stream()
                .map(CandidateMapper::toDTO)
                .collect(Collectors.toList());
    }
}
