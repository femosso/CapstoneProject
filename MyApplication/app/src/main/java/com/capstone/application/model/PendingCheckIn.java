package com.capstone.application.model;

public class PendingCheckIn {
    private long id;
    private long date;

    public PendingCheckIn(Long id, long date) {
        this.id = id;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
