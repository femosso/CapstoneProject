
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.server.dao.QuestionDao;
import com.capstone.server.model.Question;

@Repository
public class JpaQuestionDao implements QuestionDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void persist(Question question) {
        em.persist(question);
    }

    @Transactional
    public Question update(Question question) {
        return em.merge(question);
    }

    @Transactional
    public Question remove(String email) {
        Question obj = find(email);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public Question find(String email) {
        return em.find(Question.class, email);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<Question> findAll() {
        Query query = em.createQuery("SELECT e FROM Question e");
        return (Collection<Question>) query.getResultList();
    }
}
