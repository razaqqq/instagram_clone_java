package com.example.instagram_clone_2017.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Users implements Parcelable {
  private String user_id;
  private String phone_number;
  private String email;
  private String username;
  private String password;

    public Users(String user_id, String phone_number, String email, String username, String password) {
        this.user_id = user_id;
        this.phone_number = phone_number;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public Users(String user_id, String phone_number, String email, String username) {
        this.user_id = user_id;
        this.phone_number = phone_number;
        this.email = email;
        this.username = username;
    }


    public Users(String email, String phone_number) {
        this.phone_number = phone_number;
        this.email = email;
    }

    public Users() {

    }

    protected Users(Parcel in) {
        user_id = in.readString();
        phone_number = in.readString();
        email = in.readString();
        username = in.readString();
        password = in.readString();
    }

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }
        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Users{" +
                "user_id='" + user_id + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(user_id);
        parcel.writeString(phone_number);
        parcel.writeString(email);
        parcel.writeString(username);
        parcel.writeString(password);
    }
}
