package cn.ctcraft.ctonlinereward.pojo;

import cn.ctcraft.ctonlinereward.pojo.annotation.ConfigMapper;
import org.bukkit.OfflinePlayer;

public class RewardInDatabase {
    private String player_uuid;
    @ConfigMapper(key = "reward_Id", setFunction = "setReward_Id")
    private String reward_Id;
    @ConfigMapper(key = "received_at", setFunction = "setReceived_at")
    private Long received_at;

    public RewardInDatabase(String player_uuid, String reward_Id, Long received_at) {
        this.player_uuid = player_uuid;
        this.reward_Id = reward_Id;
        this.received_at = received_at;
    }

    public RewardInDatabase() {
    }

    public String getPlayer_uuid() {
        return player_uuid;
    }

    public void setPlayer_uuid(String player_uuid) {
        this.player_uuid = player_uuid;
    }

    public String getReward_Id() {
        return reward_Id;
    }

    public void setReward_Id(String reward_Id) {
        this.reward_Id = reward_Id;
    }

    public Long getReceived_at() {
        return received_at;
    }

    public void setReceived_at(Long received_at) {
        this.received_at = received_at;
    }

    @Override
    public String toString() {
        return "RewardInDatabase{" +
                "player_uuid='" + player_uuid + '\'' +
                ", reward_Id='" + reward_Id + '\'' +
                ", received_at=" + received_at +
                '}';
    }
}
