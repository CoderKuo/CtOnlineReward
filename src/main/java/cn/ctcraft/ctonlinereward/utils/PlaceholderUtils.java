package cn.ctcraft.ctonlinereward.utils;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class PlaceholderUtils {
    public static PlaceholderUtils instance = new PlaceholderUtils();

    private PlaceholderUtils() {
    }

    public static PlaceholderUtils getInstance() {
        return instance;
    }

    private Configuration config;

    public void loadPlaceholderConfigToMemory() {
        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        try {
            File file = new File(plugin.getDataFolder(), "placeholder.yml");
            if (!file.exists()) {
                plugin.saveResource("placeholder.yml", false);
            }
            config = YamlConfiguration.loadConfiguration(file);
            plugin.getLogger().info("placeholder.yml加载成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertVar(String var) {

    }

    public String getValue(String key, Object... values) {
        return String.format(config.getString(key), values);
    }


}
