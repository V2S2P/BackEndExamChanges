package app.populator;

import app.config.HibernateConfig;
import app.daos.CandidateDAO;
import app.daos.SkillDAO;
import app.entities.Candidate;
import app.entities.Skill;
import app.enums.Category;
import jakarta.persistence.EntityManagerFactory;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class Populator {

    private final CandidateDAO candidateDAO;
    private final SkillDAO skillDAO;

    public Populator(EntityManagerFactory emf) {
        this.candidateDAO = new CandidateDAO(emf);
        this.skillDAO = new SkillDAO(emf);
    }

    public void populate() {
        // 1️⃣ Create skills with slugs
        Skill java = createSkill("Java", Category.PROG_LANG, "General-purpose programming language", "java");
        Skill python = createSkill("Python", Category.PROG_LANG, "General-purpose programming language", "python");
        Skill postgre = createSkill("PostgreSQL", Category.DB, "Relational database system", "postgresql");
        Skill docker = createSkill("Docker", Category.DEVOPS, "Containerization tool", "docker");
        Skill html = createSkill("HTML", Category.FRONTEND, "Markup language for web pages", "html");
        Skill junit = createSkill("JUnit", Category.TESTING, "Java testing framework", "junit");
        Skill pandas = createSkill("Pandas", Category.DATA, "Python data analysis library", "pandas");
        Skill spring = createSkill("Spring Boot", Category.FRAMEWORK, "Java web framework", "spring-boot");

        List<Skill> allSkills = List.of(java, python, postgre, docker, html, junit, pandas, spring);

        // 2️⃣ Create candidates
        Candidate alice = createCandidate("Alice Johnson", "555-1234", "BSc Computer Science");
        Candidate bob = createCandidate("Bob Smith", "555-5678", "MSc Software Engineering");
        Candidate carol = createCandidate("Carol Lee", "555-8765", "BSc Information Systems");

        // 3️⃣ Link skills to candidates
        alice.addSkill(java);
        alice.addSkill(spring);
        alice.addSkill(python);

        bob.addSkill(docker);
        bob.addSkill(postgre);
        bob.addSkill(junit);

        carol.addSkill(html);
        carol.addSkill(pandas);
        carol.addSkill(python);

        List<Candidate> allCandidates = List.of(alice, bob, carol);

        // 4️⃣ Persist skills first (required for ManyToMany)
        allSkills.forEach(skillDAO::create);

        // 5️⃣ Persist candidates
        allCandidates.forEach(candidateDAO::create);

        System.out.println("Database populated with sample skills and candidates with slugs!");
    }

    private Skill createSkill(String name, Category category, String description, String slug) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(category);
        skill.setDescription(description);
        skill.setSlug(slug != null ? slug : generateSlug(name));
        return skill;
    }

    private String generateSlug(String input) {
        // Convert to lowercase, remove accents, replace spaces with hyphens
        String slug = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-$", "")
                .replaceAll("^-", "");
        return slug;
    }

    private Candidate createCandidate(String name, String phone, String education) {
        Candidate candidate = new Candidate();
        candidate.setName(name);
        candidate.setPhone(phone);
        candidate.setEducationBackground(education);
        candidate.setSkills(new ArrayList<>()); // initialize empty list
        return candidate;
    }

    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        Populator populator = new Populator(emf);
        populator.populate();
    }
}
