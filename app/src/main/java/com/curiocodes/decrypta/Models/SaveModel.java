package com.curiocodes.decrypta.Models;

import java.util.Date;

public class SaveModel {
    private String uri,sender;
    private Date date;

    public SaveModel(){}

    public SaveModel(String uri, String sender, Date date) {
        this.uri = uri;
        this.sender = sender;
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public String getUri() {
        return uri;
    }

    public Date getDate() {
        return date;
    }
}
