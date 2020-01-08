package com.curiocodes.decrypta.Models;

public class RecentChatModel {
    private String name;
    private String number;
    private String uri;

    public RecentChatModel(){}

    public RecentChatModel(String name, String number, String uri) {
        this.name = name;
        this.number = number;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getUri() {
        return uri;
    }
}
