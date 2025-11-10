package app.services;

import app.dtos.SkillStatsDTO;

import java.util.List;
import java.util.stream.Collectors;

public class MockSkillStatsService extends SkillStatsService {

    @Override
    public List<SkillStatsDTO> getSkillStats(List<String> slugs) {
        // Return mock data for all requested slugs
        return slugs.stream().map(slug -> {
            SkillStatsDTO dto = new SkillStatsDTO();
            dto.setSlug(slug);
            dto.setName("Mock " + slug);
            dto.setPopularityScore(100);  // fixed mock popularity
            dto.setAverageSalary(50000);  // fixed mock salary
            return dto;
        }).collect(Collectors.toList());
    }
}
