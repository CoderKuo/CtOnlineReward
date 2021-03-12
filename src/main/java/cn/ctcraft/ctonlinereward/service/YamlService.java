package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.utils.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

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
                ctOnlineReward.saveResource("gui/extendMenu.yml", false);
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
            loadRemindJson();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ctOnlineReward.getLogger().warning("§c§l■ reward.yml数据读取失败!");
        }
        return false;
    }

    public boolean loadRemindJson() {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        Set<String> keys = rewardYaml.getKeys(false);
        for (String key : keys) {
            ConfigurationSection configurationSection = rewardYaml.getConfigurationSection(key);
            Set<String> keys1 = configurationSection.getKeys(false);
            if (keys1.contains("remind")) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("reward",new JsonPrimitive(key));
                JsonElement jsonElement = new JsonPrimitive(configurationSection.getBoolean("remind", false));
                jsonObject.add("remind", jsonElement);
                if (keys1.contains("permission")) {
                    JsonElement jsonElement1 = new JsonPrimitive(configurationSection.getString("permission"));
                    jsonObject.add("permission", jsonElement1);
                }
                YamlData.remindJson.add(jsonObject);
            }
        }
        return true;
    }
}
