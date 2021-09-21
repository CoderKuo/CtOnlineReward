package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.service.YamlService;
import cn.ctcraft.ctonlinereward.utils.Util;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class YamlBase implements DataService {
    private final CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private AbstractMap.Entry<String,YamlConfiguration> yamlDataPair = null;
    private final ReadWriteLock readWriteLock=new ReentrantReadWriteLock(true);
    private final Lock readLock=readWriteLock.readLock();
    private final Lock writeLock=readWriteLock.writeLock();
    public YamlConfiguration getYamlData(){
        readLock.lock();
        YamlConfiguration pastYamlDataPair=null;
        try {
            File dataFolder = new File(ctOnlineReward.getDataFolder() + "/playerData");
            if (!dataFolder.exists()) {
                boolean mkdir = dataFolder.mkdir();
                if (mkdir) {
                    ctOnlineReward.getLogger().info("§a§l● 玩家数据文件夹构建成功!");
                } else {
                    return null;
                }
            }
            String date = Util.getDate();
            //比较日期，未初始化或日期已更新即进行初始化并保存数据
            if (yamlDataPair == null || !yamlDataPair.getKey().equals(date)) {
                File file = new File(ctOnlineReward.getDataFolder() + "/playerData/" + date + ".yml");
                YamlConfiguration yamlConfiguration = new YamlConfiguration();
                if (!file.exists()) {
                    try {
                        boolean newFile = file.createNewFile();
                        if (!newFile) {
                            ctOnlineReward.getLogger().warning("§c§l■ 玩家数据创建失败!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        yamlConfiguration.load(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (yamlDataPair != null) {
                    //避免死锁 在锁外保存数据
                    pastYamlDataPair=yamlDataPair.getValue();
                }
                yamlDataPair = new AbstractMap.SimpleEntry<>(date, yamlConfiguration);
            }
        }finally {
            readLock.unlock();
            //避免死锁 在锁外保存数据
            if(pastYamlDataPair!=null){
                saveData(pastYamlDataPair);
            }
        }
        return yamlDataPair.getValue();
    }

    private void saveData(YamlConfiguration yamlConfiguration){
        String s = yamlDataPair.getKey();
        File file = new File(ctOnlineReward.getDataFolder() + "/playerData/" + s + ".yml");
        writeLock.lock();
        try {
            yamlConfiguration.save(file);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            writeLock.unlock();
        }

    }


    public int getPlayerOnlineTime(Player pLayer){
        YamlConfiguration playerData = getYamlData();
        return playerData.getInt(pLayer.getUniqueId().toString()+".time");
    }

    public void addPlayerOnlineTime(Player player,int time){
        YamlConfiguration playerData = getYamlData();
        playerData.set(player.getUniqueId().toString()+".time",time);
        saveData(playerData);
    }

    @Override
    public void insertPlayerOnlineTime(Player player,int time) {
        YamlConfiguration playerData = getYamlData();
        playerData.set(player.getUniqueId().toString()+".time",time);
        saveData(playerData);
    }

    public List<String> getPlayerRewardArray(Player player){
        YamlConfiguration playerData = getYamlData();
        return playerData.getStringList(player.getUniqueId().toString() + ".reward");
    }

    public boolean addRewardToPlayData(String rewardId,Player player){
        YamlConfiguration playerData = getYamlData();
        List<String> rewardList = playerData.getStringList(player.getUniqueId().toString() + ".reward");
        rewardList.add(rewardId);
        playerData.set(player.getUniqueId().toString()+".reward",rewardList);
        saveData(playerData);
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


    public class FilterBySuffix implements FilenameFilter {
        private String suffix;

        public FilterBySuffix(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(suffix);
        }
    }

}

