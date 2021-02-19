package cn.ctcraft.ctonlinereward.service.afk;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AfkTimer extends BukkitRunnable {
    private static CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private Map<String, Map<String, Double>> locationMap = new HashMap<>();
    private AfkService afkService = AfkService.getInstance();
    private boolean strong = AfkService.getInstance().isStrongMode();

    @Override
    public void run() {
        FileConfiguration config = ctOnlineReward.getConfig();
        boolean use = config.getBoolean("Setting.afkConfig.use");
        if (!use) {
            return;
        }

        Set<String> uuidSet = locationMap.keySet();
        for (String uuid : uuidSet) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            if (!offlinePlayer.isOnline()) {
                locationMap.remove(uuid);
                afkService.removeAfk(uuid);
            }
        }

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayers) {
            String uuid = player.getUniqueId().toString();
            boolean b = locationMap.containsKey(uuid);
            Location location = player.getLocation();
            if (b) {
                Map<String, Double> oldLocationMap = locationMap.get(uuid);
                boolean xyz = oldLocationMap.get("x") == location.getX() && oldLocationMap.get("y") == location.getY()
                        && oldLocationMap.get("z") == location.getZ();
                boolean see = oldLocationMap.get("pitch") == location.getPitch() && oldLocationMap.get("yaw") == location.getYaw();

                if (!xyz){
                    boolean afk = afkService.isAfk(player);
                    if (afk) {
                        afkService.removeAfk(player);
                    }
                }else{
                    if (strong){
                        if (!see){
                            boolean afk = afkService.isAfk(player);
                            if (!afk) {
                                afkService.setAfk(player);
                            }
                        }
                    }
                    if (see){
                        boolean afk = afkService.isAfk(player);
                        if (!afk) {
                            afkService.setAfk(player);
                        }
                    }else{
                        if (!strong){
                            boolean afk = afkService.isAfk(player);
                            if (afk) {
                                afkService.removeAfk(player);
                            }
                        }
                    }
                }

            }

            Map<String, Double> map = new HashMap<>();
            map.put("x", location.getX());
            map.put("y", location.getY());
            map.put("z", location.getZ());
            float pitch = location.getPitch();
            map.put("pitch", (double) pitch);
            float yaw = location.getYaw();
            map.put("yaw", (double) yaw);
            locationMap.put(uuid, map);
        }


    }
}
