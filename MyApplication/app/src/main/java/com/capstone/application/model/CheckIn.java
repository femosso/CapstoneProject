package com.capstone.application.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CheckIn implements Parcelable {
    private Long id;
    private User user;
    private List<Answer> answerList;
    private long date;
    private String photoPath;

    public CheckIn() {
    }

    public Long getId() {
        return id;
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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    protected CheckIn(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readLong();
        user = (User) in.readValue(User.class.getClassLoader());
        if (in.readByte() == 0x01) {
            answerList = new ArrayList<>();
            in.readList(answerList, Answer.class.getClassLoader());
        } else {
            answerList = null;
        }
        date = in.readLong();
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
        dest.writeValue(user);
        if (answerList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(answerList);
        }
        dest.writeLong(date);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CheckIn> CREATOR = new Parcelable.Creator<CheckIn>() {
        @Override
        public CheckIn createFromParcel(Parcel in) {
            return new CheckIn(in);
        }

        @Override
        public CheckIn[] newArray(int size) {
            return new CheckIn[size];
        }
    };
}
