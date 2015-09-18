
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.User;

public interface UserDao {

    public void persist(User user);
    public User update(User user);
    public User remove(String email);
    public User find(String email);
    public Collection<User> findAll();
}