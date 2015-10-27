
package com.capstone.server.model;

import java.util.List;

public class Feedback {
    private User user;
    private List<Answer> answerList;

    public Feedback() {
    }

    public Feedback(User user, List<Answer> answerList) {
        super();
        this.user = user;
        this.answerList = answerList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Answer> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<Answer> answerList) {
        this.answerList = answerList;
    }

}
