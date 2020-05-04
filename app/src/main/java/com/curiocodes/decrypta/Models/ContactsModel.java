package com.curiocodes.decrypta.Models;

public class ContactsModel {

    private String phone;
    private String name;

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public ContactsModel(String phone, String name) {
        this.phone = phone;
        this.name = name;
    }


}
