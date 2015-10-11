package com.capstone.application.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckIn {

    private Long id;

    private Teen teen;

    private Date date;

    private Question question;

    public CheckIn(Teen teen, Question question, Date date) {
        this.teen = teen;
        this.question = question;
        this.date = date;
    }

    public Teen getTeen() {
        return teen;
    }

    public void setTeen(Teen teen) {
        this.teen = teen;
    }

    public Date getDate() {
        return date;
    }

    public String getStringDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public String getStringTime() {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}