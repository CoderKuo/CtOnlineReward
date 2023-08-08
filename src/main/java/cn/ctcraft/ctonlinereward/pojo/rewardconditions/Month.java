package cn.ctcraft.ctonlinereward.pojo.rewardconditions;

import cn.ctcraft.ctonlinereward.database.DataHandler;
import org.bukkit.OfflinePlayer;

public class Month extends RewardCondition {


    public Month(OfflinePlayer player, String param) {
        super(player, param);
        addPlaceholder("reminder", offlinePlayer -> {
            return String.valueOf(DataHandler.getInstance().getPlayerOnlineTimeMonth(offlinePlayer) - convertTime());
        });
    }

    @Override
    public String getName() {
        return "month";
    }

    @Override
    public boolean isNeedConfig() {
        return false;
    }

    @Override
    public boolean check() {
        return DataHandler.getInstance().getPlayerOnlineTimeMonth(player) >= convertTime();
    }
}
