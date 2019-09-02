package com.blikoon.rooster;

public class UserInfo {
    String name = "a";
    String password = "123456";
    String rooster = "Android";

    public UserInfo(){}
    public UserInfo(String name, String password){
        this.name = name;
        this.password = password;
    }

    public final static UserInfo DEFAULT_USER = new UserInfo();
}
