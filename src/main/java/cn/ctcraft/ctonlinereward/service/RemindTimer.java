package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.service.rewardHandler.RewardOnlineTimeHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RemindTimer extends BukkitRunnable {
    private final CtOnlineReward ctOnlineReward;
    //每轮检查时已经提醒过的玩家的名单
     public static List<Player> players = new ArrayList<>();

    public RemindTimer() {
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    }


    @Override
    public void run() {
        JsonArray remindJson = YamlData.remindJson;
        //清除提醒过的玩家的名单
        players.clear();
        for (JsonElement jsonElement : remindJson) {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            if (asJsonObject.has("remind") && asJsonObject.get("remind").getAsBoolean()) {
                String reward = asJsonObject.get("reward").getAsString();
                String permission = asJsonObject.has("permission") ? asJsonObject.get("permission").getAsString() : null;
                sendMessage(permission, reward);
            }
        }

    }

    private void sendMessage(String rewardId) {
        sendMessage(null,rewardId);
    }

    private void sendMessage(String permission, String rewardId) {
        Bukkit.getOnlinePlayers().parallelStream()
                .filter(onlinePlayer -> permission == null || onlinePlayer.hasPermission(permission))
                .filter(onlinePlayer -> !players.contains(onlinePlayer))
                .filter(onlinePlayer -> hasNotReceivedReward(onlinePlayer, rewardId))
                .forEach(onlinePlayer -> {
                    players.add(onlinePlayer);
                    FileConfiguration config = ctOnlineReward.getConfig();
                    String message = config.getString("Setting.remind.message");
                    if (message != null) {
                        if (message.startsWith("[")) {
                            BaseComponent[] parse = ComponentSerializer.parse(message);
                            onlinePlayer.spigot().sendMessage(parse);
                        } else {
                            onlinePlayer.sendMessage(message.replace("&", "§"));
                        }
                    }
                });
    }

    private boolean hasNotReceivedReward(Player player, String rewardId) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        ConfigurationSection configurationSection = rewardYaml.getConfigurationSection(rewardId);
        if (configurationSection == null || !configurationSection.contains("time")) {
            return false;
        }
        boolean timeIsOk = RewardOnlineTimeHandler.getInstance().onlineTimeIsOk(player, configurationSection.getString("time"));
        if (timeIsOk) {
            List<String> playerRewardArray = CtOnlineReward.dataService.getPlayerRewardArray(player);
            return playerRewardArray.isEmpty() || !playerRewardArray.contains(rewardId);
        }
        return false;
    }
}
