package app.daos;

import app.entities.Candidate;
import app.entities.Skill;
import app.enums.Category;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;

public class CandidateDAO implements IDAO<Candidate,Integer> {
    private final EntityManagerFactory emf;

    public CandidateDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Candidate create(Candidate candidate) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(candidate);
            em.getTransaction().commit();
            return candidate;
        }catch(Exception e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    @Override
    public Candidate getById(int id) {
        try(EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT c FROM Candidate c LEFT JOIN FETCH c.skills WHERE c.id = :id",
                    Candidate.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }catch(NoResultException ex) {
            throw new ApiException(404, "No candidate with id " + id);
        }catch (Exception e) {
            throw new ApiException(500, "Error getting candidate: " + e.getMessage());
        }
    }

    @Override
    public List<Candidate> getAll() {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            List<Candidate> allCandidates = em.createQuery("SELECT DISTINCT c FROM Candidate c ORDER BY c.id",
                    Candidate.class)
                    .getResultList();
            em.getTransaction().commit();
            return allCandidates;
        }catch(Exception e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    @Override
    public Candidate update(int id, Candidate candidate) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            // id is the identifier of the existing entity in the DB that we want to update.
            // em.find tells JPA to find an entity of type "Candidate" with the primary key being (id)
            // The returned object (existingCandidate) is managed, so anything you do inside a transaction(session), JPA will automatically detect and update the DB.
            Candidate existingCandidate = em.find(Candidate.class, id);
            if(existingCandidate == null) {
                throw new ApiException(500, "Candidate not found");
            }
            existingCandidate.setName(candidate.getName());
            existingCandidate.setSkills(candidate.getSkills());
            existingCandidate.setPhone(candidate.getPhone());
            existingCandidate.setEducationBackground(candidate.getEducationBackground());

            em.getTransaction().commit();
            return existingCandidate;
        }catch (Exception e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    @Override
    public boolean deleteById(int id) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate existingCandidate = em.find(Candidate.class, id);
            if(existingCandidate == null) {
                throw new ApiException(500, "Candidate not found");
            }
            em.remove(existingCandidate);
            em.getTransaction().commit();
            return true;
        }catch(Exception e) {
            throw new ApiException(500, e.getMessage());
        }
    }
    // Additional method: link existing skill to a candidate(potentially not needed because of helper add methods)
    public void linkSkill(int candidateId, int skillId) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate candidate = em.find(Candidate.class, candidateId);
            Skill skill = em.find(Skill.class, skillId);

            if (candidate != null && skill != null) {
                candidate.addSkill(skill);
                em.merge(candidate);
            }

            em.getTransaction().commit();
        }
    }
    public Candidate patch(int id, Candidate candidate) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Candidate existingCandidate = em.find(Candidate.class, id);
            if(existingCandidate == null) {
                throw new ApiException(500, "Candidate not found");
            }
            // Only update the fields that are non-null (sent by client)
            if (candidate.getName() != null) {
                existingCandidate.setName(candidate.getName());
            }
            if (candidate.getPhone() != null) {
                existingCandidate.setPhone(candidate.getPhone());
            }
            if (candidate.getEducationBackground() != null) {
                existingCandidate.setEducationBackground(candidate.getEducationBackground());
            }
            if (candidate.getSkills() != null && !candidate.getSkills().isEmpty()) {
                existingCandidate.setSkills(candidate.getSkills());
            }
            em.getTransaction().commit();
            return existingCandidate;
        }catch(Exception e) {
            throw new ApiException(500, e.getMessage());
        }
    }
    public List<Candidate> getByCategory(Category category) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            List<Candidate> candidates = em.createQuery(
                            "SELECT DISTINCT c FROM Candidate c " +
                                    "JOIN FETCH c.skills s " +
                                    "WHERE s.category = :category " +
                                    "ORDER BY c.id",
                            Candidate.class)
                    .setParameter("category", category)
                    .getResultList();
            em.getTransaction().commit();
            return candidates;
        } catch (Exception e) {
            throw new ApiException(500, e.getMessage());
        }
    }
}
