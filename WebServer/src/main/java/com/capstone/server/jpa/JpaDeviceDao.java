
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.server.dao.DeviceDao;
import com.capstone.server.model.Device;

@Repository
public class JpaDeviceDao implements DeviceDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void persist(Device device) {
        em.persist(device);
    }

    @Transactional
    public Device update(Device device) {
        return em.merge(device);
    }

    @Transactional
    public Device remove(String email) {
        Device obj = find(email);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public Device removeByToken(String token) {
        Device obj = findByToken(token);
        if (obj != null) {
            em.remove(obj);
        }
        return obj;
    }

    @Transactional
    public Device find(String email) {
        return em.find(Device.class, email);
    }

    @Transactional
    public Device findByToken(String token) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<Device> criteriaQuery = builder.createQuery(Device.class);
        Root<Device> deviceRoot = criteriaQuery.from(Device.class);

        criteriaQuery.select(deviceRoot);
        criteriaQuery.where(builder.equal(deviceRoot.get("token"), token));

        Device device = null;
        try {
            device = em.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        return device;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<Device> findAll() {
        Query query = em.createQuery("SELECT e FROM Device e");
        return (Collection<Device>) query.getResultList();
    }
}
