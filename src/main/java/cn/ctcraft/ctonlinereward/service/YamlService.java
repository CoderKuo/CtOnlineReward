package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.service.json.JsonObject;
import cn.ctcraft.ctonlinereward.service.scheduler.RemindTimer;
import cn.ctcraft.ctonlinereward.utils.JsonUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        Map<String, ConfigurationSection> rewards = new ConcurrentHashMap<>();
        File rewardDir = new File(ctOnlineReward.getDataFolder(), "rewards");
        if (!rewardDir.exists()) {
            saveResourceFile("rewards/10min.yml");
        }
        try {
            Arrays.stream(Objects.requireNonNull(rewardDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".yml");
                }
            }))).map(YamlConfiguration::loadConfiguration).forEach(yamlConfiguration -> {
                yamlConfiguration.getKeys(false).forEach(key -> {
                    rewards.put(key, yamlConfiguration.getConfigurationSection(key));
                    loadRemindJson(yamlConfiguration.getConfigurationSection(key), key);
                    ctOnlineReward.getLogger().info(key + " 奖励文件加载成功!");
                });
            });
            RewardService.getInstance().loadRewardYamlToMemory(rewards);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ctOnlineReward.getLogger().warning("§c§l■ reward.yml数据读取失败!");
        }
        return false;
    }

    public void loadRemindJson(ConfigurationSection section, String key) {
        YamlData.remindJson = JsonUtils.newJsonArray();
        if (section.getBoolean("remind", false)) {
            JsonObject jsonObject = JsonUtils.newJsonObject();
            jsonObject.put("reward", key);
            jsonObject.put("remind", true);
            if (section.contains("permission")) {
                String permission = section.getString("permission");
                jsonObject.put("permission", permission);
            }
            RemindTimer.remindJson.addFromJsonString(jsonObject.toJsonString(false));
        }
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
