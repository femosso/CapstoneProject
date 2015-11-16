
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.CheckIn;

public interface CheckInDao {
    public void persist(CheckIn checkIn);
    public CheckIn update(CheckIn checkIn);
    public CheckIn remove(long id);
    public CheckIn find(long id);
    public CheckIn find(long id, boolean force);
    public Collection<CheckIn> findAll();
    public Collection<CheckIn> findAll(boolean force);
    public Collection<CheckIn> findByTeen(String email);
}