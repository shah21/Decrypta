package com.curiocodes.decrypta.Models;

import java.util.Date;

public class UserModel {
    private String name;
    private String phone;
    private String profile;
    private Date signedDate;
    private String type;

    public UserModel() {
    }

    public UserModel(String name, String phone, String profile, Date signedDate, String type) {
        this.name = name;
        this.phone = phone;
        this.profile = profile;
        this.signedDate = signedDate;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public Date getSignedDate() {
        return signedDate;
    }

    public String getType() {
        return type;
    }

    public String getProfile() {
        return profile;
    }
}
