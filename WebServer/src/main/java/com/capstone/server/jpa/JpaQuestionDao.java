
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Hibernate;
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
    public Question remove(long id) {
        Question obj = find(id);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public Question find(long id) {
        return find(id, false);
    }

    @Transactional
    public Question find(long id, boolean forceLoad) {
        Question question = em.find(Question.class, id);
        if(question != null && forceLoad) {
            Hibernate.initialize(question.getAlternativeList());
        }
        return question;
    }

    @Transactional
    public Collection<Question> findAll() {
        return findAll(false);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<Question> findAll(boolean forceLoad) {
        Query query = em.createQuery("SELECT e FROM Question e");

        Collection<Question> questions = (Collection<Question>) query.getResultList();
        if(questions != null && forceLoad) {
            for(Question question : questions) {
                Hibernate.initialize(question.getAlternativeList());
            }
        }
        return questions;
    }
}
