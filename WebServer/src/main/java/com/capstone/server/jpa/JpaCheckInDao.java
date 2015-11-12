
package com.capstone.server.jpa;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.server.dao.CheckInDao;
import com.capstone.server.model.Answer;
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
    public CheckIn remove(long id) {
        CheckIn obj = find(id);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public CheckIn find(long id) {
        return find(id, false);
    }

    @Transactional
    public CheckIn find(long id, boolean forceLoad) {
        CheckIn checkIn = em.find(CheckIn.class, id);
        if (checkIn != null && forceLoad) {
            initializeCheckIn(checkIn);
        }

        return checkIn;
    }

    @Transactional
    public Collection<CheckIn> findAll() {
        return findAll(false);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<CheckIn> findAll(boolean forceLoad) {
        Query query = em.createQuery("SELECT e FROM CheckIn e");

        Collection<CheckIn> checkIns = (Collection<CheckIn>) query.getResultList();
        if (checkIns != null && forceLoad) {
            for (CheckIn checkIn : checkIns) {
                initializeCheckIn(checkIn);
            }
        }

        return checkIns;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<CheckIn> findByTeen(String email) {
        Query query = em.createQuery(
                "SELECT e FROM CheckIn e where e.user.email=:arg1");
        query.setParameter("arg1", email);

        Collection<CheckIn> checkIns = (Collection<CheckIn>) query.getResultList();
        if(checkIns != null) {
            for(CheckIn checkIn : checkIns) {
                initializeCheckIn(checkIn);
            }
        }
        return checkIns;
    }

    public void initializeCheckIn(CheckIn checkIn) {
        Hibernate.initialize(checkIn.getUser());

        // initialize list of answers
        List<Answer> answerList = checkIn.getAnswerList();
        Hibernate.initialize(answerList);

        // initialize list of such answers' questions
        for(Answer answer : answerList) {
            Hibernate.initialize(answer.getQuestion());
        }
    }
}
