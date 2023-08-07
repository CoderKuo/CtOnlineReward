package cn.ctcraft.ctonlinereward.service.scheduler;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.DataHandler;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.pojo.OnlineRemind;
import cn.ctcraft.ctonlinereward.service.WeekOnlineRankService;
import cn.ctcraft.ctonlinereward.utils.ConfigUtil;
import cn.ctcraft.ctonlinereward.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;

public class OnlineTimer extends BukkitRunnable {
    private static OnlineTimer instance = new OnlineTimer();
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    private OnlineTimer() {
    }

    public static OnlineTimer getInstance() {
        return instance;
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
        for (Player player : onlinePlayers) {
            if (YamlData.timeLimit[0] != -1) {
                long nowTime = System.currentTimeMillis();
                int i = Util.timestampToHours(nowTime);
                if (i < YamlData.timeLimit[0] || i > YamlData.timeLimit[1]) {
                    DataHandler.getInstance().playerLevel(player);
                    continue;
                }
            }

//            if (AfkService.getInstance().isAfk(player)) {
//                continue;
//            }

//
//            int numMinutes = dataService.getPlayerOnlineTime(player);
//            long playerOnlineTime = onlinePlayerTime.get(player.getUniqueId());
//            long timePast =  System.currentTimeMillis() - playerOnlineTime;
//            if (timePast > 60 * 1000) {
//                numMinutes += ((Long) (timePast / (60 * 1000))).intValue();
//                dataService.addPlayerOnlineTime(player, numMinutes);
//
//                long newTime = System.currentTimeMillis() - (timePast % 60000);
//                onlinePlayerTime.put(player.getUniqueId(), newTime);
//            }


            boolean onlineRemind = ctOnlineReward.getConfig().getBoolean("Setting.onlineRemind.use");
            if (onlineRemind) {
                try {
                    List<OnlineRemind> objectList = ConfigUtil.getObjectList(ctOnlineReward.getConfig(), "Setting.onlineRemind.remindValues", OnlineRemind.class);
                    for (OnlineRemind remind : objectList) {
                        if (DataHandler.getInstance().getPlayerOnlineTime(player) == remind.getOnlineTime()) {
                            player.sendMessage(remind.getMessage().replace("&", "§"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            boolean weekRankEnable = ctOnlineReward.getConfig().getBoolean("Setting.weekRankEnable");
            if (weekRankEnable){
                WeekOnlineRankService.refreshList();
            }
        }
    }
}
