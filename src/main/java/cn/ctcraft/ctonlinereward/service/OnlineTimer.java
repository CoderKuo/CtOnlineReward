package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.DataService;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.pojo.OnlineRemind;
import cn.ctcraft.ctonlinereward.service.afk.AfkService;
import cn.ctcraft.ctonlinereward.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

public class OnlineTimer extends BukkitRunnable {
    private static OnlineTimer instance = new OnlineTimer();
    private DataService dataService = CtOnlineReward.dataService;
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private OnlineTimer(){
    }
    public static OnlineTimer getInstance() {
        return instance;
    }


    @Override
    public void run() {
        Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
        for (Player player : onlinePlayers) {
            boolean afk = AfkService.getInstance().isAfk(player);
            if (afk){
                return;
            }
            int playerOnlineTime = dataService.getPlayerOnlineTime(player);
            int i = playerOnlineTime + 1;
            dataService.addPlayerOnlineTime(player,i);
            boolean onlineRemind = ctOnlineReward.getConfig().getBoolean("Setting.onlineRemind.use");
            if (onlineRemind){
                try {
                    List<OnlineRemind> objectList = ConfigUtil.getObjectList(ctOnlineReward.getConfig(), "Setting.onlineRemind.remindValues", OnlineRemind.class);
                    for (OnlineRemind remind : objectList) {
                        if (i == remind.getOnlineTime()){
                            player.sendMessage(remind.getMessage().replace("&","§"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
