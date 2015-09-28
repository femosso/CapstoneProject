
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.Question;

public interface QuestionDao {

    public void persist(Question question);
    public Question update(Question question);
    public Question remove(String email);
    public Question find(String email);
    public Collection<Question> findAll();
}