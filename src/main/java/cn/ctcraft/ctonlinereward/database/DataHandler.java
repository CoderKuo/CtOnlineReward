package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.pojo.RewardInDatabase;
import cn.ctcraft.ctonlinereward.utils.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataHandler implements DataService {
    private final CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private DataService dataService = CtOnlineReward.dataService;
    private static DataHandler instance = new DataHandler();
    private Map<String, Long> playerJoinTime = new ConcurrentHashMap<>();

    private DataHandler() {
    }

    public static DataHandler getInstance() {
        return instance;
    }

    public void playerJoin(OfflinePlayer player) {
        playerJoinTime.put(player.getUniqueId().toString(), System.currentTimeMillis());
    }

    public void playerLevel(OfflinePlayer player) {
        if (!playerJoinTime.containsKey(player.getUniqueId().toString())) {
            return;
        }
        insertPlayerOnlineTime(player, playerJoinTime.get(player.getUniqueId().toString()), System.currentTimeMillis());
    }

    @Override
    public int getPlayerOnlineTime(OfflinePlayer player) {
        if (playerJoinTime.containsKey(player.getUniqueId().toString())) {
            boolean sameDay = Util.isSameDay(playerJoinTime.get(player.getUniqueId().toString()), System.currentTimeMillis());
            if (sameDay) {
                return Integer.parseInt(Util.timeDiff(playerJoinTime.get(player.getUniqueId().toString()), System.currentTimeMillis())) + dataService.getPlayerOnlineTime(player);
            } else {
                return (int) Util.calculateMinutesSinceMidnight(System.currentTimeMillis());
            }
        }
        return dataService.getPlayerOnlineTime(player);
    }

    @Override
    public void insertPlayerOnlineTime(OfflinePlayer player, long loginTime, long logoutTime) {
        dataService.insertPlayerOnlineTime(player, loginTime, logoutTime);
    }



    @Override
    public List<RewardInDatabase> getPlayerRewardArray(OfflinePlayer player, long start, long end) {
        return dataService.getPlayerRewardArray(player, start, end);
    }

    @Override
    public boolean addRewardToPlayData(String rewardId, Player player) {
        return dataService.addRewardToPlayData(rewardId, player);
    }

    @Override
    public int getPlayerOnlineTimeWeek(OfflinePlayer player) {
        int i = Integer.parseInt(Util.timeDiff(playerJoinTime.get(player.getUniqueId().toString()), System.currentTimeMillis())) + dataService.getPlayerOnlineTime(player);
        return dataService.getPlayerOnlineTimeWeek(player) + i;
    }

    @Override
    public int getPlayerOnlineTimeMonth(OfflinePlayer player) {
        int i = Integer.parseInt(Util.timeDiff(playerJoinTime.get(player.getUniqueId().toString()), System.currentTimeMillis())) + dataService.getPlayerOnlineTime(player);
        return dataService.getPlayerOnlineTimeMonth(player) + i;
    }

    @Override
    public int getPlayerOnlineTimeAll(OfflinePlayer player) {
        int i = Integer.parseInt(Util.timeDiff(playerJoinTime.get(player.getUniqueId().toString()), System.currentTimeMillis())) + dataService.getPlayerOnlineTime(player);
        return dataService.getPlayerOnlineTimeAll(player) + i;
    }

    @Override
    public int getPlayerOnlineTimeFromRange(OfflinePlayer player, Long start, Long end) {
        int i = Integer.parseInt(Util.timeDiff(playerJoinTime.get(player.getUniqueId().toString()), System.currentTimeMillis())) + dataService.getPlayerOnlineTime(player);
        return dataService.getPlayerOnlineTimeFromRange(player, start, end) + i;
    }


    @Override
    public void flush() {
        dataService.flush();
    }
}
