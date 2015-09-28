
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.CheckIn;

public interface CheckInDao {

    public void persist(CheckIn checkIn);
    public CheckIn update(CheckIn checkIn);
    public CheckIn remove(String email);
    public CheckIn find(String email);
    public Collection<CheckIn> findAll();
}