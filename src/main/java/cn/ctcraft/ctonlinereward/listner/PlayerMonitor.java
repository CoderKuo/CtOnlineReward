package cn.ctcraft.ctonlinereward.listner;

import cn.ctcraft.ctonlinereward.service.OnlineTimer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerMonitor implements Listener {
    @EventHandler
    public void joinMonitor(PlayerJoinEvent event) {
        OnlineTimer.addOnlinePlayer(event.getPlayer(), System.currentTimeMillis());
    }

    @EventHandler
    public void quitMonitor(PlayerQuitEvent event) {
        OnlineTimer.removeOnlinePlayer(event.getPlayer());
    }
}
