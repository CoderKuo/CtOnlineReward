package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.utils.Util;
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

}
