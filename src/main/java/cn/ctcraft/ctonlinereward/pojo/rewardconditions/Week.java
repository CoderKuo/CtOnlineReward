package cn.ctcraft.ctonlinereward.pojo.rewardconditions;

import cn.ctcraft.ctonlinereward.database.DataHandler;
import org.bukkit.OfflinePlayer;

public class Week extends RewardCondition {

    public Week(OfflinePlayer player, String param) {
        super(player, param);
        addPlaceholder("reminder", offlinePlayer -> {
            return String.valueOf(DataHandler.getInstance().getPlayerOnlineTimeWeek(offlinePlayer) - convertTime());
        });
    }

    @Override
    public String getName() {
        return "week";
    }

    @Override
    public boolean isNeedConfig() {
        return false;
    }

    @Override
    public boolean check() {
        return DataHandler.getInstance().getPlayerOnlineTimeWeek(player) >= convertTime();
    }
}
