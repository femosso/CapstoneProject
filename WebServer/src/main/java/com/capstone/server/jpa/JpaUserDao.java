
package com.capstone.server.jpa;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.server.dao.UserDao;
import com.capstone.server.model.Follower;
import com.capstone.server.model.Teen;
import com.capstone.server.model.User;
import com.capstone.server.utils.Constants.UserType;

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
        return find(email, false);
    }

    @Transactional
    public User find(String email, boolean forceLoad) {
        User user = em.find(User.class, email);
        if (user != null && forceLoad) {
            if (user.getType() == UserType.TEEN.ordinal()) {
                Hibernate.initialize(user.getTeen());

                // initialize list of pending follower request and its internal fields
                List<Follower> pendingFollowerList = user.getTeen().getPendingFollowerList();
                Hibernate.initialize(pendingFollowerList);
                for (Follower follower : pendingFollowerList) {
                    Hibernate.initialize(follower.getUser());
                }

                // initialize list of followers and its internal fields
                List<Follower> followerList = user.getTeen().getFollowerList();
                Hibernate.initialize(followerList);
                for (Follower follower : followerList) {
                    Hibernate.initialize(follower.getUser());
                }

                initializeFollower(user);
            } else if (user.getType() == UserType.FOLLOWER.ordinal()) {
                initializeFollower(user);
            }
            Hibernate.initialize(user.getDevice());
        }
        return user;
    }

    public void initializeFollower(User user) {
        Hibernate.initialize(user.getFollower());

        // initialize list of pending teens request and its internal fields
        List<Teen> pendingTeenList = user.getFollower().getPendingTeenList();
        Hibernate.initialize(pendingTeenList);
        for (Teen teen : pendingTeenList) {
            Hibernate.initialize(teen.getUser());
        }

        // initialize list of teens and its internal fields
        List<Teen> teenList = user.getFollower().getTeenList();
        Hibernate.initialize(teenList);
        for (Teen teen : teenList) {
            Hibernate.initialize(teen.getUser());
        }
        Hibernate.initialize(user.getFollower().getPendingTeenList());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<User> findByType(int type) {
        Query query = em.createQuery("SELECT e FROM User e where e.type=:arg1");
        query.setParameter("arg1", type);
        return (Collection<User>) query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<User> findAll() {
        // get all users in database except from the admin
        Query query = em.createQuery("SELECT e FROM User e where not e.type=:arg1");
        query.setParameter("arg1", 0);
        return (Collection<User>) query.getResultList();
    }
}
