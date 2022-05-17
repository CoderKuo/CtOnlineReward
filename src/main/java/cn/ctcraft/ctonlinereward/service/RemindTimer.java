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
import org.apache.commons.lang.ObjectUtils;
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
            if (asJsonObject.has("remind")) {
                if (asJsonObject.get("remind").getAsBoolean()) {
                    String reward = asJsonObject.get("reward").getAsString();
                    if (asJsonObject.has("permission")) {
                        sendMessage(asJsonObject.get("permission").getAsString(),reward);
                    }else {
                        sendMessage(reward);
                    }
                }

            }
        }

    }

    private void sendMessage(String rewardId) {
        sendMessage(null,rewardId);
    }

    private void sendMessage(String permission, String rewardId) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player onlinePlayer : onlinePlayers) {
            //玩家没有权限则跳过
            if (permission != null && !onlinePlayer.hasPermission(permission)){
                continue;
            }
            //该轮已经提醒过了则跳过
            if (players.contains(onlinePlayer)){
                continue;
            }
            boolean b = hasNotReceivedReward(onlinePlayer, rewardId);
            if (b) {
                //把玩家添加到提醒过的玩家的列表
                players.add(onlinePlayer);
                FileConfiguration config = ctOnlineReward.getConfig();
                String message = config.getString("Setting.remind.message");
                JsonElement parse = new JsonParser().parse(message);
                boolean jsonObject = parse.isJsonNull();
                if (!jsonObject) {
                    BaseComponent[] parse1 = ComponentSerializer.parse(message);
                    for (BaseComponent baseComponent : parse1) {
                        onlinePlayer.spigot().sendMessage(baseComponent);
                    }
                }else{
                    onlinePlayer.sendMessage(message.replace("&","§"));
                }
            }
        }
    }

    private boolean hasNotReceivedReward(Player player, String rewardId) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        ConfigurationSection configurationSection = rewardYaml.getConfigurationSection(rewardId);
        Set<String> keys = configurationSection.getKeys(false);
        if (!keys.contains("time")){
            return false;
        }
        boolean timeIsOk = RewardOnlineTimeHandler.getInstance().onlineTimeIsOk(player, configurationSection.getString("time"));
        if (timeIsOk){
            List<String> playerRewardArray = CtOnlineReward.dataService.getPlayerRewardArray(player);
            if(playerRewardArray.size() == 0){
                return true;
            }
            return !playerRewardArray.contains(rewardId);
        }
        return false;
    }
}
