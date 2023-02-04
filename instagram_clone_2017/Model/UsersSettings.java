package com.example.instagram_clone_2017.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class UsersSettings implements Parcelable {
    private String username;
    private String display_name;
    private int followers;
    private int followings;
    private int posts;
    private String profile_photo;
    private String website;
    private String descriptions;
    private String user_id;

    public UsersSettings(String username, String display_name, int followers,
                         int followings, int posts, String profile_photo,
                         String website, String descriptions, String user_id) {
        this.username = username;
        this.display_name = display_name;
        this.followers = followers;
        this.followings = followings;
        this.posts = posts;
        this.profile_photo = profile_photo;
        this.website = website;
        this.descriptions = descriptions;
        this.user_id = user_id;
    }

    public UsersSettings(String username, String display_name, String website, String descriptions) {
        this.username = username;
        this.display_name = display_name;
        this.website = website;
        this.descriptions = descriptions;
    }

    public UsersSettings() {

    }

    protected UsersSettings(Parcel in) {
        username = in.readString();
        display_name = in.readString();
        followers = in.readInt();
        followings = in.readInt();
        posts = in.readInt();
        profile_photo = in.readString();
        website = in.readString();
        descriptions = in.readString();
        user_id = in.readString();
    }

    public static final Creator<UsersSettings> CREATOR = new Creator<UsersSettings>() {
        @Override
        public UsersSettings createFromParcel(Parcel in) {
            return new UsersSettings(in);
        }

        @Override
        public UsersSettings[] newArray(int size) {
            return new UsersSettings[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowings() {
        return followings;
    }

    public void setFollowings(int followings) {
        this.followings = followings;
    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    @Override
    public String toString() {
        return "UsersSettings{" +
                "username='" + username + '\'' +
                ", display_name='" + display_name + '\'' +
                ", followers=" + followers +
                ", followings=" + followings +
                ", posts=" + posts +
                ", profile_photo='" + profile_photo + '\'' +
                ", website='" + website + '\'' +
                ", descriptions='" + descriptions + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeString(display_name);
        parcel.writeInt(followers);
        parcel.writeInt(followings);
        parcel.writeInt(posts);
        parcel.writeString(profile_photo);
        parcel.writeString(website);
        parcel.writeString(descriptions);
        parcel.writeString(user_id);
    }
}
