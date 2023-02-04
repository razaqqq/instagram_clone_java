package com.example.instagram_clone_2017.Model;

public class UsersModified {
    private Users users;
    private UsersSettings usersSettings;

    public UsersModified(UsersSettings usersSettings, Users users) {
        this.usersSettings = usersSettings;
        this.users = users;
    }

    public UsersModified() {

    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public UsersSettings getUsersSettings() {
        return usersSettings;
    }

    public void setUsersSettings(UsersSettings usersSettings) {
        this.usersSettings = usersSettings;
    }

    @Override
    public String toString() {
        return "UsersModified{" +
                "users=" + users +
                ", usersSettings=" + usersSettings +
                '}';
    }
}
