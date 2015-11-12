package com.capstone.application.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Alternative implements Parcelable {
    private Long id;

    private Question question;

    private String text;

    public Alternative() {
    }

    public Long getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    protected Alternative(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readLong();
        question = (Question) in.readValue(Question.class.getClassLoader());
        text = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(id);
        }
        dest.writeValue(question);
        dest.writeString(text);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Alternative> CREATOR = new Parcelable.Creator<Alternative>() {
        @Override
        public Alternative createFromParcel(Parcel in) {
            return new Alternative(in);
        }

        @Override
        public Alternative[] newArray(int size) {
            return new Alternative[size];
        }
    };
}