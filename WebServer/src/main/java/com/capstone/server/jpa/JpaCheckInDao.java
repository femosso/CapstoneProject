
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.server.dao.CheckInDao;
import com.capstone.server.model.CheckIn;

@Repository
public class JpaCheckInDao implements CheckInDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void persist(CheckIn checkIn) {
        em.persist(checkIn);
    }

    @Transactional
    public CheckIn update(CheckIn checkIn) {
        return em.merge(checkIn);
    }

    @Transactional
    public CheckIn remove(String email) {
        CheckIn obj = find(email);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public CheckIn find(String email) {
        return em.find(CheckIn.class, email);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<CheckIn> findAll() {
        Query query = em.createQuery("SELECT e FROM CheckIn e");
        return (Collection<CheckIn>) query.getResultList();
    }
}
