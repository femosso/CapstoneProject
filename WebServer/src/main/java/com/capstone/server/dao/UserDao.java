
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.User;

public interface UserDao {

    public void persist(User user);
    public User update(User user);
    public User remove(String email);
    public User find(String email);
    public User find(String email, boolean forceLoad);
    public Collection<User> findByType(int type);
    public Collection<User> findAll();
}