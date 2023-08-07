package cn.ctcraft.ctonlinereward.listner;

import cn.ctcraft.ctonlinereward.database.DataHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerMonitor implements Listener {
    @EventHandler
    public void joinMonitor(PlayerJoinEvent event) {
        DataHandler.getInstance().playerJoin(event.getPlayer());
    }

    @EventHandler
    public void quitMonitor(PlayerQuitEvent event) {
        DataHandler.getInstance().playerLevel(event.getPlayer());
    }
}
