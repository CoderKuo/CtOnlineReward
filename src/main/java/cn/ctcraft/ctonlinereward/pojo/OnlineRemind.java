package cn.ctcraft.ctonlinereward.pojo;

import cn.ctcraft.ctonlinereward.pojo.annotation.ConfigMapper;

public class OnlineRemind {
    @ConfigMapper("OnlineTime")
    private int onlineTime;
    @ConfigMapper("Message")
    private String message;

    public OnlineRemind() {
    }

    public OnlineRemind(int onlineTime, String message) {
        this.onlineTime = onlineTime;
        this.message = message;
    }

    public int getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(int onlineTime) {
        this.onlineTime = onlineTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OnlineRemind{" +
                "onlineTime=" + onlineTime +
                ", message='" + message + '\'' +
                '}';
    }
}
