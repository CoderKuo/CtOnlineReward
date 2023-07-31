package cn.ctcraft.ctonlinereward.pojo.rewardconditions;

import cn.ctcraft.ctonlinereward.database.DataHandler;
import org.bukkit.OfflinePlayer;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Interval extends RewardCondition {

    public Interval(OfflinePlayer player, String param) {
        super(player, param);
    }

    @Override
    public String getName() {
        return "interval";
    }

    @Override
    boolean check() {
        addFunction(configuration -> DataHandler.getInstance().getPlayerRewardArray(player, subtractNDays(convertTime()), System.currentTimeMillis()).contains(configuration.getName()));
        return true;
    }

    public static long subtractNDays(int nDays) {
        LocalDateTime currentDateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Duration duration = Duration.ofDays(nDays);
        LocalDateTime subtractedDateTime = currentDateTime.minus(duration);
        return subtractedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
