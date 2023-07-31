package cn.ctcraft.ctonlinereward.service.afk;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.DataHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AfkService {
    private static AfkService instance = new AfkService();
    private static boolean strong = false;
    private List<String> playerAfkList = new ArrayList<>();
    private AfkService(){}

    public static AfkService getInstance() {
        return instance;
    }

    public boolean isAfk(Player player){
        return playerAfkList.contains(player.getUniqueId().toString());
    }

    public void setAfk(Player player){
        boolean contains = playerAfkList.contains(player.getUniqueId().toString());
        if (!contains){
            playerAfkList.add(player.getUniqueId().toString());
            CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
            player.sendMessage(plugin.getConfig().getString("Setting.afkConfig.message.joinAfk").replace("&", "§"));
            DataHandler.getInstance().playerLevel(player);
        }
    }

    public void removeAfk(Player player){
        removeAfk(player.getUniqueId().toString());
    }

    public void removeAfk(String uuid){
        playerAfkList.remove(uuid);
        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        DataHandler.getInstance().playerJoin(offlinePlayer);
        if (offlinePlayer.isOnline()){
            Player player = offlinePlayer.getPlayer();
            player.sendMessage(plugin.getConfig().getString("Setting.afkConfig.message.levelAfk").replace("&","§"));
        }
    }


    public void openStrongMode(){
        strong = true;
    }

    public boolean isStrongMode(){
        return strong;
    }

}
