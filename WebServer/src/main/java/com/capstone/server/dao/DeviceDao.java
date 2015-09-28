
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.Device;

public interface DeviceDao {

    public void persist(Device device);
    public Device update(Device device);
    public Device remove(String email);
    public Device find(String email);
    public Collection<Device> findAll();
}