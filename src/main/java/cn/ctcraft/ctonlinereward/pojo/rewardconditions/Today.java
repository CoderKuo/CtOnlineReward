package cn.ctcraft.ctonlinereward.pojo.rewardconditions;

import cn.ctcraft.ctonlinereward.database.DataHandler;
import org.bukkit.OfflinePlayer;

public class Today extends RewardCondition {


    public Today(OfflinePlayer player, String param) {
        super(player, param);
        addPlaceholder("remainder", player1 -> {
            return String.valueOf(DataHandler.getInstance().getPlayerOnlineTime(player) - convertTime());
        });
    }


    @Override
    public boolean check() {
        return DataHandler.getInstance().getPlayerOnlineTime(player) >= convertTime();
    }

    @Override
    public String getName() {
        return "today";
    }

    @Override
    public boolean isNeedConfig() {
        return false;
    }

}
