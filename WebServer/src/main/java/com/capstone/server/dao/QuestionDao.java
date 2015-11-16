
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.Question;

public interface QuestionDao {
    public void persist(Question question);
    public Question update(Question question);
    public Question remove(long id);
    public Question find(long id);
    public Question find(long id, boolean forceLoad);
    public Collection<Question> findAll();
    public Collection<Question> findAll(boolean forceLoad);
}