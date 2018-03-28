package com.nero_oren.s1mple;

/**
 * Created by NERO on 3/17/2018.
 */

public class Chats {

    private String user_status;

    public Chats(){

    }

    public Chats(String user_status) {
        this.user_status = user_status;
    }

    public String getUser_status() {
        return user_status;
    }

    public void setUser_status(String user_status) {
        this.user_status = user_status;
    }
}
