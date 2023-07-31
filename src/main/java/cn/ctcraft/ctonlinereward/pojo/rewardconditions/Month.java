package cn.ctcraft.ctonlinereward.pojo.rewardconditions;

import cn.ctcraft.ctonlinereward.database.DataHandler;
import org.bukkit.OfflinePlayer;

public class Month extends RewardCondition {


    public Month(OfflinePlayer player, String param) {
        super(player, param);
    }

    @Override
    public String getName() {
        return "month";
    }

    @Override
    boolean check() {
        return DataHandler.getInstance().getPlayerOnlineTimeMonth(player) >= convertTime();
    }
}
