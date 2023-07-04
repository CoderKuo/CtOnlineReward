package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WeekOnlineRankService {
    public static List<JsonObject> list = new ArrayList<>();

    public static JsonObject getRankPlayer(int index){
        if (list.size() > index){
            return list.get(index);
        }else{
            if (index > Bukkit.getOfflinePlayers().length){
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("name",new JsonPrimitive("未上榜"));
                jsonObject.add("time",new JsonPrimitive("未上榜"));
                return jsonObject;
            }
            refreshList();
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("name",new JsonPrimitive("NaN"));
            jsonObject.add("time",new JsonPrimitive("请稍后再次请求"));
            return jsonObject;
        }
    }

    public static void refreshList(){
        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        new BukkitRunnable(){
            @Override
            public void run() {
                List<JsonObject> alist = new ArrayList<>();
                for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    FileConfiguration config = plugin.getConfig();
                    List<String> stringList = config.getStringList("Setting.weekRankFilter");
                    if (stringList.contains(offlinePlayer.getName())){
                        continue;
                    }
                    int playerOnlineTimeWeek = CtOnlineReward.dataService.getPlayerOnlineTimeWeek(offlinePlayer);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("name",new JsonPrimitive(offlinePlayer.getName()));
                    jsonObject.add("time",new JsonPrimitive(playerOnlineTimeWeek));
                    alist.add(jsonObject);
                }
                list = alist.stream().sorted(Comparator.comparing(WeekOnlineRankService::getRank).reversed()).collect(Collectors.toList());
            }
        }.runTaskAsynchronously(plugin);
    }

    public static int getRank(JsonObject json){
        return json.get("time").getAsInt();
    }
}