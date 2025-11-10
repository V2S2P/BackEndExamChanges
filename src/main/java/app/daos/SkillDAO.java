package app.daos;

import app.entities.Skill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class SkillDAO implements IDAO<Skill,Integer> {
    private final EntityManagerFactory emf;

    public SkillDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Skill create(Skill skill) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(skill);
            em.getTransaction().commit();
            return skill;
        }
    }

    public Skill getById(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT s FROM Skill s LEFT JOIN FETCH s.candidates WHERE s.id = :id", Skill.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Skill> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT s FROM Skill s", Skill.class)
                    .getResultList();
        }
    }

    @Override
    public Skill update(int id, Skill updatedSkill) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Skill existing = em.find(Skill.class, id);
            if (existing != null) {
                existing.setName(updatedSkill.getName());
                existing.setDescription(updatedSkill.getDescription());
                existing.setCategory(updatedSkill.getCategory());
                em.merge(existing);
            }
            em.getTransaction().commit();
            return existing;
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Skill skill = em.find(Skill.class, id);
            if (skill != null) {
                em.remove(skill);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        }
    }
}
