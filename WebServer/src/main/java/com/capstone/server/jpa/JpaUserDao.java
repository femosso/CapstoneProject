
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.server.dao.UserDao;
import com.capstone.server.model.User;

@Repository
public class JpaUserDao implements UserDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void persist(User user) {
        em.persist(user);
    }

    @Transactional
    public User update(User user) {
        return em.merge(user);
    }

    @Transactional
    public User remove(String email) {
        User obj = find(email);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public User find(String email) {
        return em.find(User.class, email);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<User> findAll() {
        Query query = em.createQuery("SELECT e FROM User e");
        return (Collection<User>) query.getResultList();
    }
}
