package com.capstone.application.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckIn {

    private User mUser;

    private Date mDate;

    private Question mQuestion;

    public CheckIn(User user, Question question, Date date) {
        mUser = user;
        mQuestion = question;
        mDate = date;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        this.mUser = user;
    }

    public Date getDate() {
        return mDate;
    }

    public String getStringDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(mDate);
    }

    public String getStringTime() {
        return new SimpleDateFormat("HH:mm:ss").format(mDate);
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public void setQuestion(Question question) {
        this.mQuestion = question;
    }
}