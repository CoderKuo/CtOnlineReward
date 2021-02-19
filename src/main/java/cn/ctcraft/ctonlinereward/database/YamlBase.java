package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.service.YamlService;
import cn.ctcraft.ctonlinereward.utils.Util;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
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

    @Override
    public int getPlayerOnlineTimeWeek(Player player) {
        int onlineTime = 0;
        List<String> weekString = Util.getWeekString();
        for (String s : weekString) {
            File file = new File(ctOnlineReward.getDataFolder() + "/playerData/" + s+".yml");
            if (file.exists()){
                YamlConfiguration yamlConfiguration = new YamlConfiguration();
                try {
                    yamlConfiguration.load(file);
                    int time = yamlConfiguration.getInt(player.getUniqueId().toString() + ".time");
                    onlineTime += time;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return onlineTime;
    }

    @Override
    public int getPlayerOnlineTimeMonth(Player player) {
        int onlineTime = 0;
        List<String> monthString = Util.getMonthString();
        for (String s : monthString) {
            File file = new File(ctOnlineReward.getDataFolder() + "/playerData/" + s+".yml");
            if (file.exists()){
                YamlConfiguration yamlConfiguration = new YamlConfiguration();
                try {
                    yamlConfiguration.load(file);
                    int time = yamlConfiguration.getInt(player.getUniqueId().toString() + ".time");
                    onlineTime += time;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return onlineTime;
    }

    @Override
    public int getPlayerOnlineTimeAll(Player player) {
        int onlineTime = 0;
        File file = new File(ctOnlineReward.getDataFolder() + "/playerData");
        FilterBySuffix filter = new FilterBySuffix(".yml");
        File[] files = file.listFiles(filter);
        if (files == null){
            return 0;
        }
        for (File file1 : files) {
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            try {
                yamlConfiguration.load(file1);
                int time = yamlConfiguration.getInt(player.getUniqueId().toString() + ".time");
                onlineTime += time;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return onlineTime;
    }

}

class FilterBySuffix implements FilenameFilter {
    private String suffix;

    public FilterBySuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(suffix);
    }
}
