package app.config;

import app.services.CandidateService;

import app.services.SkillService;

import jakarta.persistence.EntityManagerFactory;

public class ServiceRegistry {
    //We create one CandidateService and can inject it wherever it is needed
    public CandidateService candidateService;
    public final SkillService skillService;

    // EMF is injected into each service
    public ServiceRegistry(EntityManagerFactory emf) {
        this.candidateService = new CandidateService(emf);
        this.skillService = new SkillService(emf);
    }
    // Setter for test mode
    public void setCandidateService(CandidateService candidateService) {
        this.candidateService = candidateService;
    }
}
