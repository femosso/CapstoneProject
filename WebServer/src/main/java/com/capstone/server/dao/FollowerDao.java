
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.Follower;

public interface FollowerDao {
    public void persist(Follower follower);
    public Follower update(Follower follower);
    public Follower remove(String email);
    public Follower find(String email);
    public Collection<Follower> findAll();
}