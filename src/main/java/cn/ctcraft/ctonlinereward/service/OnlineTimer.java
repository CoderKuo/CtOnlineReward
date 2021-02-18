package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.DataService;
import cn.ctcraft.ctonlinereward.database.YamlData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.Collection;

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
            int playerOnlineTime = dataService.getPlayerOnlineTime(player);
            dataService.addPlayerOnlineTime(player,playerOnlineTime+1);
        }
    }
}
