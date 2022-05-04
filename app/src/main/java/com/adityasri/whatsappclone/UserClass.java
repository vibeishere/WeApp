package com.adityasri.whatsappclone;

public class UserClass {
    String name,status,image,thumbImage;

    public UserClass(){

    }

    public UserClass(String name, String status, String image,String thumbImage) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumbImage = thumbImage;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public void setThumbImage(String thumbImage) {
        this.thumbImage = thumbImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
