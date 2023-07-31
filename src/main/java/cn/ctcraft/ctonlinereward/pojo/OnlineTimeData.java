package cn.ctcraft.ctonlinereward.pojo;

import cn.ctcraft.ctonlinereward.pojo.annotation.ConfigMapper;
import org.bukkit.OfflinePlayer;

public class OnlineTimeData {
    private OfflinePlayer player;
    @ConfigMapper(key = "login_time", setFunction = "setLoginTime")
    private long loginTime;
    @ConfigMapper(key = "logout_time", setFunction = "setLogoutTime")
    private long logoutTime;

    public OnlineTimeData(OfflinePlayer player, long loginTime, long logoutTime) {
        this.player = player;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
    }

    public OnlineTimeData() {
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public void setPlayer(OfflinePlayer player) {
        this.player = player;
    }

    public String getUUIDString() {
        return player.getUniqueId().toString();
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public long getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(long logoutTime) {
        this.logoutTime = logoutTime;
    }

    @Override
    public String toString() {
        return "OnlineTimeData{" +
                "loginTime=" + loginTime +
                ", logoutTime=" + logoutTime +
                '}';
    }
}
