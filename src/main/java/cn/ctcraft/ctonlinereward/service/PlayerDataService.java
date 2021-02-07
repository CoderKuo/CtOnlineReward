package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.YamlData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerDataService {
    private static PlayerDataService instance = new PlayerDataService();
    private PlayerDataService(){}

    public static PlayerDataService getInstance() {
        return instance;
    }

    public int getPlayerOnlineTime(Player pLayer){
        YamlConfiguration playerData = YamlData.playerData;
        return playerData.getInt(pLayer.getUniqueId().toString()+".time");
    }

    public List<String> getPlayerRewardArray(Player player){
        YamlService instance = YamlService.getInstance();
        boolean b = instance.loadPlayerDataYaml();
        if(!b){
            CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
            plugin.getLogger().warning("§c§l■ 玩家数据文件读取失败!");
        }
        YamlConfiguration playerData = YamlData.playerData;
        return playerData.getStringList(player.getUniqueId().toString() + ".reward");
    }

    public boolean addRewardToPlayData(String rewardId,Player player){
        YamlService instance = YamlService.getInstance();
        boolean b = instance.loadPlayerDataYaml();
        if(!b){
            CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
            plugin.getLogger().warning("§c§l■ 玩家数据文件读取失败!");
            return false;
        }
        YamlConfiguration playerData = YamlData.playerData;
        List<String> rewardList = playerData.getStringList(player.getUniqueId().toString() + ".reward");
        rewardList.add(rewardId);
        playerData.set(player.getUniqueId().toString()+".reward",rewardList);
        instance.saveData(playerData);
        return true;
    }



}
