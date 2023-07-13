package cn.ctcraft.ctonlinereward.database;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface DataService {

    int getPlayerOnlineTime(OfflinePlayer pLayer);
    void addPlayerOnlineTime(OfflinePlayer player,int time);
    void insertPlayerOnlineTime(OfflinePlayer player,int time);
    List<String> getPlayerRewardArray(OfflinePlayer player);
    boolean addRewardToPlayData(String rewardId,Player player);

    int getPlayerOnlineTimeWeek(OfflinePlayer player);
    int getPlayerOnlineTimeMonth(OfflinePlayer player);
    int getPlayerOnlineTimeAll(OfflinePlayer player);


}
