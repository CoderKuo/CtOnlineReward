package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
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
    private YamlService yamlService = YamlService.getInstance();
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
            boolean b = yamlService.loadPlayerDataYaml();
            if(!b){
                ctOnlineReward.getLogger().warning("§c§l■ 玩家数据文件获取失败!");
            }
            YamlConfiguration playerData = YamlData.playerData;
            int anInt = playerData.getInt(player.getUniqueId().toString()+".time");
            playerData.set(player.getUniqueId().toString()+".time",anInt+1);
            yamlService.saveData(playerData);
        }
    }
}
