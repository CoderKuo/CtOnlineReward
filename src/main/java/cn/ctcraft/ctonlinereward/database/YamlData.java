package cn.ctcraft.ctonlinereward.database;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class YamlData {
    public static Map<String, YamlConfiguration> guiYaml = new HashMap<>();
    public static YamlConfiguration rewardYaml = new YamlConfiguration();
    public static YamlConfiguration playerData = new YamlConfiguration();

    public static Map<Player,Long> playerJoinTime = new HashMap<>();
}
