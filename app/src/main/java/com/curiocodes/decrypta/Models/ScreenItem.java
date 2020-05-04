package com.curiocodes.decrypta.Models;

public class ScreenItem {
    private String title, desc;
    int screenImg;

    public ScreenItem(String title, String desc, int screenImg) {
        this.title = title;
        this.desc = desc;
        this.screenImg = screenImg;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setScreenImg(int screenImg) {
        this.screenImg = screenImg;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public int getScreenImg() {
        return screenImg;
    }
}
