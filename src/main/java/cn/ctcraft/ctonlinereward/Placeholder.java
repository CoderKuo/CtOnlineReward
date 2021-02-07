package cn.ctcraft.ctonlinereward;

import cn.ctcraft.ctonlinereward.service.PlayerDataService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholder extends PlaceholderExpansion {
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private PlayerDataService playerDataService = PlayerDataService.getInstance();
    private static Placeholder instance = new Placeholder();
    private Placeholder(){
        
    }

    public static Placeholder getInstance() {
        return instance;
    }


    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        switch (params){
            case "player":
                return player.getName();
            case "world":
                return player.getWorld().getName();
            case "onlineTime":
                return String.valueOf(playerDataService.getPlayerOnlineTime(player));

        }
        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "CtOnlineReward";
    }

    @Override
    public @NotNull String getAuthor() {
        return "大阔";
    }

    @Override
    public @NotNull String getVersion() {
        return ctOnlineReward.getDescription().getVersion();
    }
}
