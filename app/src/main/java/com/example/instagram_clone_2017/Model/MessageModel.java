package com.example.instagram_clone_2017.Model;

public class MessageModel {
    String userId, message, messageId;
    Long timeStamp;


    public MessageModel(String userId, String message, Long timeStamp) {
        this.userId = userId;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public MessageModel(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }



    public MessageModel()
    {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
