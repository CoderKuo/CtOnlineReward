package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.DataService;
import cn.ctcraft.ctonlinereward.pojo.OnlineRemind;
import cn.ctcraft.ctonlinereward.service.afk.AfkService;
import cn.ctcraft.ctonlinereward.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class OnlineTimer extends BukkitRunnable {
    private static OnlineTimer instance = new OnlineTimer();
    private static HashMap<UUID, Long> onlinePlayerTime = new HashMap<>();
    private DataService dataService = CtOnlineReward.dataService;
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    private OnlineTimer() {
        //插件可能在服务器正常开启后启用 此时手动添加在线玩家
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            addOnlinePlayer(onlinePlayer,onlinePlayer.getLastPlayed());
        }
    }

    public static OnlineTimer getInstance() {
        return instance;
    }

    public static void addOnlinePlayer(Player player, Long time) {
        onlinePlayerTime.put(player.getUniqueId(), time);
    }

    public static void removeOnlinePlayer(Player player) {
        onlinePlayerTime.remove(player.getUniqueId());
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
        for (Player player : onlinePlayers) {
            if (!onlinePlayerTime.containsKey(player.getUniqueId())) {
                continue;
            }
            if (AfkService.getInstance().isAfk(player)) {
                continue;
            }

            int numMinutes = dataService.getPlayerOnlineTime(player);
            long playerOnlineTime = onlinePlayerTime.get(player.getUniqueId());
            long timePast = System.currentTimeMillis() - playerOnlineTime;
            if (timePast > 60 * 1000) {
                numMinutes += ((Long) (timePast / (60 * 1000))).intValue();
                dataService.addPlayerOnlineTime(player, numMinutes);

                long newTime = System.currentTimeMillis() - (timePast % 60000);
                onlinePlayerTime.put(player.getUniqueId(), newTime);
            }

            boolean onlineRemind = ctOnlineReward.getConfig().getBoolean("Setting.onlineRemind.use");
            if (onlineRemind) {
                try {
                    List<OnlineRemind> objectList = ConfigUtil.getObjectList(ctOnlineReward.getConfig(), "Setting.onlineRemind.remindValues", OnlineRemind.class);
                    for (OnlineRemind remind : objectList) {
                        if (numMinutes == remind.getOnlineTime()) {
                            player.sendMessage(remind.getMessage().replace("&", "§"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
