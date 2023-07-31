package cn.ctcraft.ctonlinereward.pojo.rewardconditions;

import cn.ctcraft.ctonlinereward.database.DataHandler;
import org.bukkit.OfflinePlayer;

public class Week extends RewardCondition {

    public Week(OfflinePlayer player, String param) {
        super(player, param);
    }

    @Override
    public String getName() {
        return "week";
    }

    @Override
    boolean check() {
        return DataHandler.getInstance().getPlayerOnlineTimeWeek(player) >= convertTime();
    }
}
