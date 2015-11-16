
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.Device;

public interface DeviceDao {
    public void persist(Device device);
    public Device update(Device device);
    public Device remove(String email);
    public Device removeByToken(String token);
    public Device find(String email);
    public Device findByToken(String token);
    public Collection<Device> findAll();
}