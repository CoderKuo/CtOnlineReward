package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.service.YamlService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class YamlBase implements DataService {
    private YamlService yamlService = YamlService.getInstance();
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    public YamlBase(){
        yamlService.loadPlayerDataYaml();
    }


    public int getPlayerOnlineTime(Player pLayer){
        boolean b = yamlService.loadPlayerDataYaml();
        if (!b){
            ctOnlineReward.getLogger().warning("§c§l■ 玩家数据读取异常!");
        }
        YamlConfiguration playerData = YamlData.playerData;
        return playerData.getInt(pLayer.getUniqueId().toString()+".time");
    }

    public void addPlayerOnlineTime(Player player,int time){
        boolean b = yamlService.loadPlayerDataYaml();
        if (!b){
            ctOnlineReward.getLogger().warning("§c§l■ 玩家数据读取异常!");
        }
        YamlConfiguration playerData = YamlData.playerData;
        playerData.set(player.getUniqueId().toString()+".time",time);
        yamlService.saveData(playerData);
    }

    @Override
    public void insertPlayerOnlineTime(Player player,int time) {
        boolean b = yamlService.loadPlayerDataYaml();
        if (!b){
            ctOnlineReward.getLogger().warning("§c§l■ 玩家数据读取异常!");
        }
        YamlConfiguration playerData = YamlData.playerData;
        playerData.set(player.getUniqueId().toString()+".time",time);
        yamlService.saveData(playerData);
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
