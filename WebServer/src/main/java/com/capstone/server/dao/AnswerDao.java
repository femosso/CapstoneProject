
package com.capstone.server.dao;

import java.util.Collection;

import com.capstone.server.model.Answer;

public interface AnswerDao {

    public void persist(Answer answer);
    public Answer update(Answer answer);
    public Answer remove(long id);
    public Answer find(long id);
    public Collection<Answer> findAll();
    public Collection<Answer> findByTeenAndType(String email, String type);
}