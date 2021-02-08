package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.YamlData;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class YamlService {
    private static YamlService instance = new YamlService();
    private CtOnlineReward ctOnlineReward;

    private YamlService() {
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    }

    public static YamlService getInstance() {
        return instance;
    }

    public boolean loadGuiYaml() throws IOException, InvalidConfigurationException {
        Map<String, YamlConfiguration> guiYaml = YamlData.guiYaml;
        File file = new File(ctOnlineReward.getDataFolder() + "/gui");
        if (!file.exists()) {
            boolean mkdir = file.mkdir();
            if (mkdir) {
                ctOnlineReward.saveResource("gui/menu.yml", false);
                ctOnlineReward.saveResource("gui/extendMenu.yml",false);
                ctOnlineReward.getLogger().info("§a§l● GUI文件夹构建成功!");
            }
        }
        File[] files = file.listFiles((File pathname) -> pathname.getName().contains(".yml"));
        if (files == null) {
            return false;
        }

        for (File file1 : files) {
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.load(file1);
            guiYaml.put(file1.getName(), yamlConfiguration);
        }

        boolean b = guiYaml.containsKey("menu.yml");
        if (!b) {
            ctOnlineReward.getLogger().warning("§c§l■ 未找到menu.yml菜单文件,即将自动生成菜单文件!");
            ctOnlineReward.saveResource("gui/menu.yml", false);
            loadGuiYaml();
        }


        return true;
    }

    public boolean loadRewardYaml() {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        File file = new File(ctOnlineReward.getDataFolder() + "/reward.yml");
        if (!file.exists()) {
            ctOnlineReward.saveResource("reward.yml", false);
        }
        try {
            rewardYaml.load(file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ctOnlineReward.getLogger().warning("§c§l■ reward.yml数据读取失败!");
        }
        return false;
    }

    public boolean loadPlayerDataYaml() {
        File playerDataFile = getPlayerDataFile();
        if (playerDataFile == null) {
            return false;
        }
        try {
            if (!playerDataFile.exists()) {
                boolean newFile = playerDataFile.createNewFile();
                if (newFile) {
                    ctOnlineReward.getLogger().info("§a§l● 玩家数据文件构建成功!");
                } else {
                    return false;
                }
            }
            YamlData.playerData = new YamlConfiguration();
            YamlData.playerData.load(playerDataFile);
            return true;
        } catch (InvalidConfigurationException e) {
            YamlData.playerData = new YamlConfiguration();
            return true;
        } catch (Exception e) {
            ctOnlineReward.getLogger().warning("§c§l■ 玩家数据文件读取失败!");
            e.printStackTrace();
            return false;
        }
    }

    public void saveData(YamlConfiguration yaml) {
        File playerDataFile = getPlayerDataFile();
        if (playerDataFile == null) {
            ctOnlineReward.getLogger().warning("§c§l■ 玩家数据文件保存失败!");
            return;
        }
        try {
            yaml.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getPlayerDataFile() {
        File dataFolder = new File(ctOnlineReward.getDataFolder() + "/playerData");
        if (!dataFolder.exists()) {
            boolean mkdir = dataFolder.mkdir();
            if (mkdir) {
                ctOnlineReward.getLogger().info("§a§l● 玩家数据文件夹构建成功!");
            } else {
                return null;
            }
        }
        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String format = simpleDateFormat.format(timeStamp);
        return new File(dataFolder + "/" + format + ".yml");
    }
}
