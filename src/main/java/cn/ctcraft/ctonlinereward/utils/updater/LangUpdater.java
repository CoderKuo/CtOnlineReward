package cn.ctcraft.ctonlinereward.utils.updater;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class LangUpdater {
    public static void exec(){
        YamlConfiguration lang = CtOnlineReward.lang;
        ConfigurationSection reward = lang.getConfigurationSection("reward");
        boolean volume3 = reward.contains("volume3");
        if (!volume3){
            CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
            plugin.saveResource("lang.yml",false);
        }
    }
}
