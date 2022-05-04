package com.adityasri.whatsappclone.sendNotificationCode;

public class Data {

    private String Title;
    private String Message;
    private String click_action = "WeApp_TARGET_NOTIFICATIONS";
    private String userId ;
    private String UID;

    public Data(String title, String message, String userId, String UID) {
        Title = title;
        Message = message;
        this.userId = userId;
        this.UID = UID;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getClick_action() {
        return click_action;
    }

    public void setClick_action(String click_action) {
        this.click_action = click_action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Data(String title, String message, String userId) {
        Title = title;
        Message = message;
        this.userId = userId;
    }

    public Data() {
    }

    public Data(String title, String message) {
        Title = title;
        Message = message;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
