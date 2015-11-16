
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.server.dao.AnswerDao;
import com.capstone.server.model.Answer;

@Repository
public class JpaAnswerDao implements AnswerDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void persist(Answer answer) {
        em.persist(answer);
    }

    @Transactional
    public Answer update(Answer answer) {
        return em.merge(answer);
    }

    @Transactional
    public Answer remove(long id) {
        Answer obj = find(id);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public Answer find(long id) {
        return em.find(Answer.class, id);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<Answer> findAll() {
        return (Collection<Answer>) em.createQuery("SELECT e FROM Answer e").getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<Answer> findByTeenAndType(String email, String type) {
        Query query = em.createQuery(
                "SELECT e FROM Answer e where e.checkIn.user.email=:arg1 and e.question.type=:arg2");
        query.setParameter("arg1", email);
        query.setParameter("arg2", type);

        Collection<Answer> answers = (Collection<Answer>) query.getResultList();
        if (answers != null) {
            for (Answer answer : answers) {
                Hibernate.initialize(answer.getCheckIn());
            }
        }
        return answers;
    }
}
