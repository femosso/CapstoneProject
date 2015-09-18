
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.server.dao.TeenDao;
import com.capstone.server.model.Teen;

@Repository
public class JpaTeenDao implements TeenDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void persist(Teen teen) {
        em.persist(teen);
    }

    @Transactional
    public Teen update(Teen teen) {
        return em.merge(teen);
    }

    @Transactional
    public Teen remove(String email) {
        Teen obj = find(email);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public Teen find(String email) {
        return em.find(Teen.class, email);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<Teen> findAll() {
        Query query = em.createQuery("SELECT e FROM Teen e");
        return (Collection<Teen>) query.getResultList();
    }
}
