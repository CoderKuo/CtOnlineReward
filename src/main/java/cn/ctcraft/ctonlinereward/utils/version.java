package cn.ctcraft.ctonlinereward.utils;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.file.YamlConfiguration;

import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class version {
    public static List<String> getVersionMsg() {
        List<String> versionMsg = new ArrayList<>();
        String version = "获取失败！";
        try {
            Path tempFile = Files.createTempFile("version", ".yml");
            Files.copy(new URL("https://note.youdao.com/yws/public/note/eead3b553997e3392d008f9787c45757").openStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            String str = new String(Files.readAllBytes(tempFile), "UTF-8");
            JsonParser jsonParser = new JsonParser();
            JsonElement parse = jsonParser.parse(str);
            JsonObject asJsonObject = parse.getAsJsonObject();
            JsonElement content = asJsonObject.get("content");
            String yamlText = content.getAsString().replace("<div yne-bulb-block=\"paragraph\" style=\"white-space: pre-wrap;\">","").replace("</div>","").replace("&nbsp;"," ").replace("<br>","\n");
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.loadFromString(yamlText);
            version = yamlConfiguration.getString("CtOnlineReward.version");
        } catch (Exception e) {
            version = "版本信息获取失败！";
            versionMsg.add(version);
            return versionMsg;
        }

        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        versionMsg.add("§6===========[CtOnlineReward]============");
        if (isNewerVersion(plugin.getDescription().getVersion(), version)) {
            versionMsg.add("CtOnlineReward不是最新版本! 最新版本: §b" + version + "§6!§f 你的版本: §b" + plugin.getDescription().getVersion());
        } else {
            versionMsg.add("欢迎您使用CtOnlineReward最新版本! 最新版本号:§b" +version+"§6!§f 您的版本号: §b" +plugin.getDescription().getVersion());
        }
        versionMsg.add("§6=======================================");
        return versionMsg;
    }

    private static boolean isNewerVersion(String currentVersion, String latestVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        for (int i = 0; i < Math.max(currentParts.length, latestParts.length); i++) {
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }

        return false;
    }


}
