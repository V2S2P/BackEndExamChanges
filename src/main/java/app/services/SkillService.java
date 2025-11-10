package app.services;

import app.daos.SkillDAO;
import app.dtos.SkillDTO;
import app.entities.Skill;
import app.exceptions.ApiException;
import app.mappers.SkillMapper;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.stream.Collectors;

public class SkillService {
    private final SkillDAO skillDAO;

    public SkillService(EntityManagerFactory emf) {
        this.skillDAO = new SkillDAO(emf);
    }

    public SkillDTO create(SkillDTO skillDTO) {
        if (skillDTO == null)
            throw new ApiException(400, "Skill data is missing");

        Skill skill = SkillMapper.toEntity(skillDTO);
        Skill created = skillDAO.create(skill);
        return SkillMapper.toDTO(created);
    }

    public SkillDTO getById(int id) {
        Skill skill = skillDAO.getById(id);
        if (skill == null) {
            throw new ApiException(404, "Skill not found");
        }
        return SkillMapper.toDTO(skill);
    }

    public List<SkillDTO> getAll() {
        return skillDAO.getAll().stream()
                .map(SkillMapper::toDTO)
                .collect(Collectors.toList());
    }

    public SkillDTO update(SkillDTO skillDTO, int id) {
        if (skillDTO == null || skillDTO.getId() == null)
            throw new ApiException(400, "Skill ID is required for update");

        Skill existingSkill = skillDAO.getById(id);
        if (existingSkill == null)
            throw new ApiException(404, "Skill not found");

        existingSkill.setName(skillDTO.getName());
        existingSkill.setCategory(skillDTO.getCategory());
        existingSkill.setDescription(skillDTO.getDescription());

        Skill updated = skillDAO.update(id, existingSkill);
        return SkillMapper.toDTO(updated);
    }

    public void delete(int id) {
        try {
            boolean deleted = skillDAO.deleteById(id);
            if (!deleted) {
                throw new ApiException(404, "Skill not found");
            }
        } catch (NoResultException e) {
            throw new ApiException(404, "Skill not found");
        }
    }
}
