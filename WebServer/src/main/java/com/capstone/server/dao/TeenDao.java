
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.Teen;

public interface TeenDao {

    public void persist(Teen teen);
    public Teen update(Teen teen);
    public Teen remove(String email);
    public Teen find(String email);
    public Collection<Teen> findAll();
}