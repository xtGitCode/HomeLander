package com.example.safetyapp.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Contacts implements Parcelable {

    String uid;
    String contactName;
    String contactPhone;

    protected Contacts(Parcel in) {
        uid = in.readString();
        contactName = in.readString();
        contactPhone = in.readString();
    }

    public static final Creator<Contacts> CREATOR = new Creator<Contacts>() {
        @Override
        public Contacts createFromParcel(Parcel in) {
            return new Contacts(in);
        }

        @Override
        public Contacts[] newArray(int size) {
            return new Contacts[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Contacts(String uid, String contactName, String contactNum) {
        this.uid = uid;
        this.contactName = contactName;
        this.contactPhone = contactNum;
    }

    public Contacts() {
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNum() {
        return contactPhone;
    }

    public void setContactNum(String contactNum) {
        this.contactPhone = contactNum;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(contactName);
        dest.writeString(contactPhone);

    }
}
