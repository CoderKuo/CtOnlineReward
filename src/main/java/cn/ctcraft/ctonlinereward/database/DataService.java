package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.pojo.RewardData;
import cn.ctcraft.ctonlinereward.pojo.RewardInDatabase;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface DataService {

    int getPlayerOnlineTime(OfflinePlayer pLayer);

    void insertPlayerOnlineTime(OfflinePlayer player, long loginTime, long logoutTime);

    List<RewardInDatabase> getPlayerRewardArray(OfflinePlayer player, long start, long end);

    boolean addRewardToPlayData(String rewardId, Player player);

    int getPlayerOnlineTimeWeek(OfflinePlayer player);

    int getPlayerOnlineTimeMonth(OfflinePlayer player);

    int getPlayerOnlineTimeAll(OfflinePlayer player);

    int getPlayerOnlineTimeFromRange(OfflinePlayer player, Long start, Long end);

    void flush();

}
