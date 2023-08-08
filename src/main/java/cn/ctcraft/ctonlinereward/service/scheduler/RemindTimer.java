package cn.ctcraft.ctonlinereward.service.scheduler;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.service.RewardService;
import cn.ctcraft.ctonlinereward.service.json.JsonArray;
import cn.ctcraft.ctonlinereward.service.json.JsonObject;
import cn.ctcraft.ctonlinereward.service.rewardHandler.RewardOnlineTimeHandler;
import cn.ctcraft.ctonlinereward.utils.JsonUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 可领取提醒
 * <p>
 * 待改造
 */
public class RemindTimer extends BukkitRunnable {
    private final CtOnlineReward ctOnlineReward;
    //每轮检查时已经提醒过的玩家的名单
    public static List<Player> players = new ArrayList<>();
    public static JsonArray remindJson = JsonUtils.newJsonArray();

    public RemindTimer() {
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    }


    @Override
    public void run() {
        //清除提醒过的玩家的名单
        players.clear();
        Iterator<JsonObject> iterator = remindJson.getIterator();
        while (iterator.hasNext()) {
            JsonObject next = iterator.next();
            if (next.has("remind") && next.getBoolean("remind")) {
                String reward = next.getString("reward");
                String permission = next.has("permission") ? next.getString("permission") : null;
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
        ConfigurationSection configurationSection = RewardService.getInstance().getRewardSection(rewardId);
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
