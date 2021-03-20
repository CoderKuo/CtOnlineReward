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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemindTimer extends BukkitRunnable {
    private final CtOnlineReward ctOnlineReward;

    public RemindTimer() {
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    }


    @Override
    public void run() {
        JsonArray remindJson = YamlData.remindJson;
        for (JsonElement jsonElement : remindJson) {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            if (asJsonObject.has("remind")) {
                boolean remind = asJsonObject.get("remind").getAsBoolean();
                if (remind) {
                    if (asJsonObject.has("permission")) {
                        sendMessage(asJsonObject.get("permission").getAsString(),asJsonObject.get("reward").getAsString());
                    }
                    sendMessage(asJsonObject.get("reward").getAsString());
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
            if (permission != null && !onlinePlayer.hasPermission(permission)){
                return;
            }
            boolean b = hasNotReceivedReward(onlinePlayer, rewardId);
            if (b) {
                FileConfiguration config = ctOnlineReward.getConfig();
                String message = config.getString("Setting.remind.message");
                JsonElement parse = new JsonParser().parse(message);
                boolean jsonObject = parse.isJsonObject();
                if (jsonObject) {
                    BaseComponent[] parse1 = ComponentSerializer.parse(message);
                    onlinePlayer.spigot().sendMessage(parse1);
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
