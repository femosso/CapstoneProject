
package com.capstone.server.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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
    public Device find(String email) {
        return em.find(Device.class, email);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Collection<Device> findAll() {
        Query query = em.createQuery("SELECT e FROM Device e");
        return (Collection<Device>) query.getResultList();
    }
}
