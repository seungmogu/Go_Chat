package com.example.go_chat;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String userName;
    public String userage;
    public String usergender;
    public static String useruid;

   private static String login_username;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


    public User(String userName, String userage, String usergender) {
        this.userName = userName;
        this.userage = userage;
        this.usergender = usergender;
    }

    public static String getUseruid() {
        return useruid;
    }

    public static void setUseruid(String useruid) {
        User.useruid = useruid;
    }

    public static String getLogin_username() {
        return login_username;
    }

    public static void setLogin_username(String login_username) {
        User.login_username = login_username;
    }


    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", userGender='" + usergender + '\'' +
                ", userage=" + userage + '\'' +
                '}';
    }

}
