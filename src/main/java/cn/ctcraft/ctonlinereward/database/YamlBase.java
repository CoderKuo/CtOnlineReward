package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.utils.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
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

    public AbstractMap.Entry<String,YamlConfiguration> getYamlData(){
        readLock.lock();
        Map.Entry<String,YamlConfiguration> pastYamlDataPair=null;
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
                    pastYamlDataPair=yamlDataPair;
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
        return yamlDataPair;
    }

    private void saveData(Map.Entry<String,YamlConfiguration> yamlDataPair){
        String s = yamlDataPair.getKey();
        File file = new File(ctOnlineReward.getDataFolder() + "/playerData/" + s + ".yml");
        writeLock.lock();
        try {
            yamlDataPair.getValue().save(file);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            writeLock.unlock();
        }

    }


    public int getPlayerOnlineTime(OfflinePlayer pLayer){
        Map.Entry<String,YamlConfiguration> playerData = getYamlData();
        return playerData.getValue().getInt(pLayer.getUniqueId().toString()+".time");
    }

    public void addPlayerOnlineTime(OfflinePlayer player,int time){
        Map.Entry<String,YamlConfiguration> playerData = getYamlData();
        playerData.getValue().set(player.getUniqueId().toString()+".time",time);
        saveData(playerData);
    }

    @Override
    public void insertPlayerOnlineTime(OfflinePlayer player,int time) {
        Map.Entry<String,YamlConfiguration> playerData = getYamlData();
        playerData.getValue().set(player.getUniqueId().toString()+".time",time);
        saveData(playerData);
    }

    public List<String> getPlayerRewardArray(OfflinePlayer player){
        Map.Entry<String,YamlConfiguration> playerDataPair = getYamlData();
        return playerDataPair.getValue().getStringList(player.getUniqueId().toString() + ".reward");
    }

    public boolean addRewardToPlayData(String rewardId,Player player){
        Map.Entry<String,YamlConfiguration> playerDataPair = getYamlData();
        YamlConfiguration playerData=playerDataPair.getValue();
        List<String> rewardList = playerData.getStringList(player.getUniqueId().toString() + ".reward");
        rewardList.add(rewardId);
        playerData.set(player.getUniqueId().toString()+".reward",rewardList);
        saveData(playerDataPair);
        return true;
    }

    @Override
    public int getPlayerOnlineTimeWeek(OfflinePlayer player) {
        int onlineTime = 0;
        List<String> weekString = Util.getWeekString();

        Map.Entry<String, YamlConfiguration> yamlData = getYamlData();

        for (String s : weekString) {
            File file = new File(ctOnlineReward.getDataFolder() + "/playerData/" + s+".yml");
            if (file.exists()){
                try {
                    int time;
                    //如果是当前文件，使用内存数据
                    if(file.getName().startsWith(yamlData.getKey())){
                        readLock.lock();
                        try {
                            time = yamlData.getValue().getInt(player.getUniqueId().toString() + ".time");
                        }finally {
                            readLock.unlock();
                        }
                    }else {
                        YamlConfiguration yamlConfiguration = new YamlConfiguration();
                        yamlConfiguration.load(file);
                        time=yamlConfiguration.getInt(player.getUniqueId().toString() + ".time");
                    }
                    onlineTime += time;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return onlineTime;
    }

    @Override
    public int getPlayerOnlineTimeMonth(OfflinePlayer player) {
        int onlineTime = 0;
        List<String> monthString = Util.getMonthString();
        Map.Entry<String, YamlConfiguration> yamlData = getYamlData();

        for (String s : monthString) {
            File file = new File(ctOnlineReward.getDataFolder() + "/playerData/" + s+".yml");
            if (file.exists()){
                try {
                    int time;
                    //如果是当前文件，使用内存数据
                    if(file.getName().startsWith(yamlData.getKey())){
                        readLock.lock();
                        try {
                            time = yamlData.getValue().getInt(player.getUniqueId().toString() + ".time");
                        }finally {
                            readLock.unlock();
                        }
                    }else {
                        YamlConfiguration yamlConfiguration = new YamlConfiguration();
                        yamlConfiguration.load(file);
                        time=yamlConfiguration.getInt(player.getUniqueId().toString() + ".time");
                    }
                    onlineTime += time;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return onlineTime;
    }

    @Override
    public int getPlayerOnlineTimeAll(OfflinePlayer player) {
        int onlineTime = 0;
        File file = new File(ctOnlineReward.getDataFolder() + "/playerData");
        File[] files = file.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null){
            return 0;
        }
        Map.Entry<String, YamlConfiguration> yamlData = getYamlData();

        for (File file1 : files) {
            try {
                int time;
                //如果是当前文件，使用内存数据
                if(file1.getName().startsWith(yamlData.getKey())){
                    readLock.lock();
                    try {
                        time = yamlData.getValue().getInt(player.getUniqueId().toString() + ".time");
                    }finally {
                        readLock.unlock();
                    }
                }else {
                    YamlConfiguration yamlConfiguration = new YamlConfiguration();
                    yamlConfiguration.load(file1);
                    time=yamlConfiguration.getInt(player.getUniqueId().toString() + ".time");
                }
                onlineTime += time;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return onlineTime;
    }

}

