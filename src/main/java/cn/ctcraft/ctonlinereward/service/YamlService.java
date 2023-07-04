package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.utils.Util;
import com.google.gson.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class YamlService {
    private static final YamlService instance = new YamlService();
    private final CtOnlineReward ctOnlineReward;

    private YamlService() {
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    }

    public static YamlService getInstance() {
        return instance;
    }

    public boolean loadGuiYaml() throws IOException, InvalidConfigurationException {
        Map<String, YamlConfiguration> guiYaml = YamlData.guiYaml;
        Path folderPath = Paths.get(ctOnlineReward.getDataFolder().getPath(), "gui");
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
            saveResourceFile("gui/menu.yml");
            saveResourceFile("gui/extendMenu.yml");
            ctOnlineReward.getLogger().info("§a§l● GUI文件夹构建成功!");
        }
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderPath, "*.yml")) {
            for (Path filePath : directoryStream) {
                YamlConfiguration yamlConfiguration = new YamlConfiguration();
                yamlConfiguration.load(filePath.toFile());
                guiYaml.put(filePath.getFileName().toString(), yamlConfiguration);
            }
        }

        boolean containsMenuYaml = guiYaml.containsKey("menu.yml");
        if (!containsMenuYaml) {
            ctOnlineReward.getLogger().warning("§c§l■ 未找到menu.yml菜单文件,即将自动生成菜单文件!");
            saveResourceFile("gui/menu.yml");
            return loadGuiYaml();
        }

        return true;
    }

    public boolean loadRewardYaml() {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        Path filePath = Paths.get(ctOnlineReward.getDataFolder().getPath(), "reward.yml");
        if (!Files.exists(filePath)) {
            saveResourceFile("reward.yml");
        }
        try {
            rewardYaml.load(filePath.toFile());
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
        YamlData.remindJson = new JsonArray();
        Set<String> keys = rewardYaml.getKeys(false);
        keys.stream()
                .filter(key -> rewardYaml.contains(key + ".remind"))
                .forEach(key -> {
                    ConfigurationSection configurationSection = rewardYaml.getConfigurationSection(key);
                    boolean remind = configurationSection.getBoolean("remind", false);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("reward", key);
                    jsonObject.addProperty("remind", remind);
                    if (configurationSection.contains("permission")) {
                        String permission = configurationSection.getString("permission");
                        jsonObject.addProperty("permission", permission);
                    }
                    YamlData.remindJson.add(jsonObject);
                });

        return true;
    }

    private void saveResourceFile(String resourcePath) {
        Path outputPath = Paths.get(ctOnlineReward.getDataFolder().getPath(), resourcePath);
        try (InputStream inputStream = ctOnlineReward.getResource(resourcePath)) {
            if (inputStream != null) {
                Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
