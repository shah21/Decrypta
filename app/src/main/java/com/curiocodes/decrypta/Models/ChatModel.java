package com.curiocodes.decrypta.Models;

import android.net.Uri;

import java.util.Date;

public class ChatModel {
    private String message;
    private String uri;
    private String sender;
    private Date time;

    public ChatModel(){}

    public ChatModel(String message, String uri, String sender, Date time) {
        this.message = message;
        this.uri = uri;
        this.sender = sender;
        this.time = time;
    }

    public String getUri() { return uri; }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public Date getTime() {
        return time;
    }
}
