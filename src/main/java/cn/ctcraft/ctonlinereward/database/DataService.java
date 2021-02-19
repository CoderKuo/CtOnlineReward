package cn.ctcraft.ctonlinereward.database;

import org.bukkit.entity.Player;

import java.util.List;

public interface DataService {

    int getPlayerOnlineTime(Player pLayer);
    void addPlayerOnlineTime(Player player,int time);
    void insertPlayerOnlineTime(Player player,int time);
    List<String> getPlayerRewardArray(Player player);
    boolean addRewardToPlayData(String rewardId,Player player);

    int getPlayerOnlineTimeWeek(Player player);
    int getPlayerOnlineTimeMonth(Player player);
    int getPlayerOnlineTimeAll(Player player);
}
