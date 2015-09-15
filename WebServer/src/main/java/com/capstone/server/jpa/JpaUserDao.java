
package com.capstone.server.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
}
