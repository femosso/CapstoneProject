
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.server.dao.FollowerDao;
import com.capstone.server.model.Follower;

@Repository
public class JpaFollowerDao implements FollowerDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void persist(Follower follower) {
        em.persist(follower);
    }

    @Transactional
    public Follower update(Follower follower) {
        return em.merge(follower);
    }

    @Transactional
    public Follower remove(String email) {
        Follower obj = find(email);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public Follower find(String email) {
        return em.find(Follower.class, email);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<Follower> findAll() {
        Query query = em.createQuery("SELECT e FROM Follower e");
        return (Collection<Follower>) query.getResultList();
    }
}
