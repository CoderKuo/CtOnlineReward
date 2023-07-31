package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.service.json.JsonArray;
import cn.ctcraft.ctonlinereward.utils.JsonUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class YamlData {
    public static Map<String, YamlConfiguration> guiYaml = new HashMap<>();
    //    public static YamlConfiguration rewardYaml = new YamlConfiguration();
    public static JsonArray remindJson = JsonUtils.newJsonArray();
    public static int[] timeLimit = new int[]{-1, -1};
}
