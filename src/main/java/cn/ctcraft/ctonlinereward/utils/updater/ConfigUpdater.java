package cn.ctcraft.ctonlinereward.utils.updater;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class ConfigUpdater {
    public void getNetWorkConfig(){
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        try {
            Path tempFile = Files.createTempFile("netWorkConfig", ".yml");
            Files.copy(new URL("https://note.youdao.com/yws/public/note/879700bc36630603c77d00e0a5091cf4").openStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            String str = new String(Files.readAllBytes(tempFile), "UTF-8");
            JsonParser jsonParser = new JsonParser();
            JsonElement parse = jsonParser.parse(str);
            JsonObject asJsonObject = parse.getAsJsonObject();
            JsonElement content = asJsonObject.get("content");
            String yamlText = content.getAsString().replace("<div yne-bulb-block=\"paragraph\" style=\"white-space: pre-wrap;\">","").replace("</div>","").replace("&nbsp;"," ").replace("<br>","\n");
            yamlConfiguration.loadFromString(yamlText);
        }catch (Exception e){
            e.printStackTrace();
        }
        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        FileConfiguration config = plugin.getConfig();
        Set<String> keys = config.getKeys(true);
        Set<String> keys1 = yamlConfiguration.getKeys(false);
        if (keys.size() != keys1.size()){
            plugin.getLogger().info("§c§l检测到配置文件错误！");
        }
        for (String s : keys1) {
            if (!keys.contains(s)){
                boolean configurationSection = yamlConfiguration.isConfigurationSection(s);
                if (!configurationSection){
                    config.set(s,yamlConfiguration.get(s));
                }
            }
        }
        if(keys.size() == keys1.size()){
            plugin.getLogger().info("§a§l配置文件更新完成！");
        }
    }
}
