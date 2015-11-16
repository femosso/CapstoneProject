
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Hibernate;
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
        return find(email, false);
    }

    @Transactional
    public Teen find(String email, boolean forceLoad) {
        Teen teen = em.find(Teen.class, email);
        if (teen != null && forceLoad) {
            Hibernate.initialize(teen.getUser());
            Hibernate.initialize(teen.getFollowerList());
            Hibernate.initialize(teen.getPendingFollowerList());
        }
        return teen;
    }

    @Transactional
    public Collection<Teen> findAll() {
        return findAll(false);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<Teen> findAll(boolean forceLoad) {
        Query query = em.createQuery("SELECT e FROM Teen e");

        Collection<Teen> teens = (Collection<Teen>) query.getResultList();
        if (teens != null && forceLoad) {
            for (Teen teen : teens) {
                Hibernate.initialize(teen.getFollowerList());
            }
        }
        return teens;
    }
}
