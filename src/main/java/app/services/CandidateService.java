package app.services;

import app.daos.CandidateDAO;
import app.daos.SkillDAO;
import app.dtos.CandidateDTO;
import app.dtos.SkillDTO;
import app.dtos.SkillStatsDTO;
import app.dtos.TopCandidateDTO;
import app.entities.Candidate;
import app.entities.Skill;
import app.enums.Category;
import app.exceptions.ApiException;
import app.mappers.CandidateMapper;
import app.mappers.SkillMapper;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.stream.Collectors;

public class CandidateService {
    private final CandidateDAO candidateDAO;
    private final SkillDAO skillDAO;
    private final SkillStatsService skillStatsService;

    public CandidateService(EntityManagerFactory emf) {
        this.candidateDAO = new CandidateDAO(emf);
        this.skillDAO = new SkillDAO(emf);
        this.skillStatsService = new SkillStatsService();
    }

    // New constructor for injecting mock service
    public CandidateService(EntityManagerFactory emf, SkillStatsService skillStatsService) {
        this.candidateDAO = new CandidateDAO(emf);
        this.skillDAO = new SkillDAO(emf);
        this.skillStatsService = skillStatsService;
    }

    public CandidateDTO create(CandidateDTO candidateDTO) {
        if (candidateDTO == null) {
            // Client sent invalid or missing input data
            throw new ApiException(400, "Candidate data is missing");
        }
        Candidate candidate = CandidateMapper.toEntity(candidateDTO);
        Candidate created = candidateDAO.create(candidate);
        return CandidateMapper.toDTO(created);
    }

    public CandidateDTO getById(int id) {
        try {
            Candidate candidate = candidateDAO.getById(id);
            if (candidate == null) {
                //We throw exception if candidate doesn't exist
                throw new ApiException(404, "Candidate not found");
            }
            return CandidateMapper.toDTO(candidate);
            //We catch the exception
        }catch (ApiException e) {
            //And throw it unchanged
            throw e;
        }catch (Exception e) {
            throw new ApiException(404, "Failed to fetch candidates: " + e.getMessage());
        }
    }

    public List<CandidateDTO> getAll() {
        return candidateDAO.getAll().stream()
                //Anonymous function: takes a Candidate 'c' and returns its corresponding CandidateDTO
                .map(c -> CandidateMapper.toDTO(c, false))
                .collect(Collectors.toList());

        /*Without using stream
        List<CandidateDTO> dtos = new ArrayList<>();
        for (Candidate c : candidateDAO.getAll()) {
            dtos.add(CandidateMapper.toDTO(c));
        }
        return dtos;
        */
    }

    public CandidateDTO update(CandidateDTO candidateDTO, int id) {
        if(candidateDTO == null || candidateDTO.getId() == null){
            throw new ApiException(400, "Candidate data is missing");
        }

        Candidate existingCandidate = candidateDAO.getById(id);
        if (existingCandidate == null) {
            throw new ApiException(404, "Candidate not found");
        }
        //Update fields from DTO
        existingCandidate.setName(candidateDTO.getName());
        existingCandidate.setEducationBackground(candidateDTO.getEducationBackground());
        existingCandidate.setPhone(candidateDTO.getPhone());

        //Update skills if provided
        if(candidateDTO.getSkills() != null && !candidateDTO.getSkills().isEmpty()){
            existingCandidate.setSkills(
                    candidateDTO.getSkills().stream()
                            .map(SkillMapper::toEntity)
                            .collect(Collectors.toList())
            );
        }
        Candidate updated = candidateDAO.update(id,existingCandidate);
        return CandidateMapper.toDTO(updated);
    }
    public void delete(int id) {
        try {
            boolean deleted = candidateDAO.deleteById(id);
            if(!deleted) {
                throw new ApiException(404, "Candidate not found");
            }
        }catch (NoResultException e) {
            throw new ApiException(404, "Candidate not found");
        }
    }
    public CandidateDTO linkSkill(int candidateId, int skillId) {
        Candidate candidate = candidateDAO.getById(candidateId);
        Skill skill = skillDAO.getById(skillId);

        if (candidate == null){
            throw new ApiException(404, "Candidate not found");
        }
        if (skill == null){
            throw new ApiException(404, "Skill not found");
        }

        candidate.addSkill(skill);
        skill.addCandidate(candidate);

        Candidate updated = candidateDAO.update(candidateId, candidate);
        return CandidateMapper.toDTO(updated);
    }
    public List<CandidateDTO> filterCandidatesByCategory(String category) {
        Category categoryEnum;
        try {
            categoryEnum = Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(400, "Invalid category: " + category);
        }
        List<Candidate> candidates = candidateDAO.getByCategory(categoryEnum);
        return candidates.stream()
                .map(c -> CandidateMapper.toDTO(c, true))
                .collect(Collectors.toList());
        /*Without using stream
        for (Candidate c : allCandidates) {
            for (Skill s : c.getSkills()) {
                if (s.getCategory() == categoryEnum)
            }
        }
         */
    }
    public CandidateDTO getByIdEnriched(int id) {
        Candidate candidate = candidateDAO.getById(id);
        CandidateDTO dto = CandidateMapper.toDTO(candidate);

        if(dto.getSkills() == null || dto.getSkills().isEmpty()) return dto;

        List<String> slugs = dto.getSkills().stream()
                .map(SkillDTO::getSlug)
                .toList();

        List<SkillStatsDTO> stats;
        try {
           stats = skillStatsService.getSkillStats(slugs);
        }catch (Exception e){
            throw new ApiException(500, "Failed to fetch skill stats: " +  e.getMessage());
        }
        dto.getSkills().forEach(skill -> {
            stats.stream()
                    .filter(s -> s.getSlug().equalsIgnoreCase(skill.getSlug()))
                    .findFirst()
                    .ifPresent(s -> {
                        skill.setPopularityScore(s.getPopularityScore());
                        skill.setAverageSalary(s.getAverageSalary());
                    });
        });

        return dto;
    }
    public TopCandidateDTO getTopCandidateByPopularityScore() {
        // 1️⃣ Fetch all candidates
        List<Candidate> allCandidates = candidateDAO.getAll();

        if (allCandidates.isEmpty()) {
            throw new ApiException(404, "No candidates found");
        }

        // 2️⃣ Map each candidate to an enriched DTO (popularityScore filled)
        List<TopCandidateDTO> candidatesWithPopularity = allCandidates.stream()
                .map(candidate -> {
                    CandidateDTO enrichedCandidate;
                    try {
                        enrichedCandidate = getByIdEnriched(candidate.getId()); // enrich skills
                    } catch (Exception e) {
                        // If enrichment fails, fallback to candidate without scores
                        enrichedCandidate = CandidateMapper.toDTO(candidate);
                    }

                    // 3️⃣ Calculate average popularity score
                    List<SkillDTO> skills = enrichedCandidate.getSkills();
                    double avgPopularity = 0.0;
                    if (skills != null && !skills.isEmpty()) {
                        avgPopularity = skills.stream()
                                .filter(skill -> skill.getPopularityScore() != null)
                                .mapToInt(SkillDTO::getPopularityScore)
                                .average()
                                .orElse(0.0);
                    }

                    return new TopCandidateDTO(candidate.getId(),(int)Math.round(avgPopularity));
                })
                .toList();

        // 4️⃣ Find the candidate with the highest average popularity
        return candidatesWithPopularity.stream()
                .max((a, b) -> Double.compare(a.getAveragePopularityScore(), b.getAveragePopularityScore()))
                .orElseThrow(() -> new ApiException(404, "No candidates found"));
    }
    public CandidateDTO patch(int id, CandidateDTO candidateDTO) {
        Candidate candidate = candidateDAO.getById(id);
        if(candidate == null) {
            throw new ApiException(404, "Candidate not found");
        }
        if (candidateDTO.getName() != null) {
            candidate.setName(candidateDTO.getName());
        }
        if (candidateDTO.getPhone() != null) {
            candidate.setPhone(candidateDTO.getPhone());
        }
        if (candidateDTO.getEducationBackground() != null) {
            candidate.setEducationBackground(candidateDTO.getEducationBackground());
        }
        if (candidateDTO.getSkills() != null && !candidateDTO.getSkills().isEmpty()) {
            candidate.setSkills(
                    candidateDTO.getSkills().stream()
                            .map(SkillMapper::toEntity)
                            .collect(Collectors.toList())
            );
        }
        Candidate updated = candidateDAO.update(id, candidate);
        return CandidateMapper.toDTO(updated);
    }

}
