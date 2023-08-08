package cn.ctcraft.ctonlinereward.pojo.rewardconditions;

import cn.ctcraft.ctonlinereward.database.DataHandler;
import cn.ctcraft.ctonlinereward.pojo.RewardInDatabase;
import cn.ctcraft.ctonlinereward.utils.PlaceholderUtils;
import cn.ctcraft.ctonlinereward.utils.Util;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;

import javax.xml.crypto.Data;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Interval extends RewardCondition implements Stateful {

    public Interval(OfflinePlayer player, String param) {
        super(player, param);
        addPlaceholder("reminder", offlinePlayer -> {
            List<RewardInDatabase> playerRewardArray = DataHandler.getInstance().getPlayerRewardArray(offlinePlayer, subtractNDays(convertTime()), System.currentTimeMillis());
            String name = getConfig().getName();

            long l = playerRewardArray.stream().filter(reward -> reward.getReward_Id().equals(name)).mapToLong(RewardInDatabase::getReceived_at).max().orElse(-1L);
            if (l == -1L) {
                return PlaceholderUtils.getInstance().getValue("interval.reminder.no-receive");
            }else {
                String s = Util.timeDiff(l, System.currentTimeMillis());
                Triple<Integer, Integer, Integer> date = Util.formatMinutesToDaysHoursMinutes(Integer.parseInt(s));
                return PlaceholderUtils.getInstance().getValue("interval.reminder.received", date.getLeft(), date.getMiddle(), date.getRight());
            }

        });

    }

    @Override
    public String getName() {
        return "interval";
    }

    @Override
    public boolean isNeedConfig() {
        return true;
    }

    @Override
    public boolean check() {
        addFunction(configuration -> DataHandler.getInstance().getPlayerRewardArray(player, subtractNDays(convertTime()), System.currentTimeMillis()).stream().anyMatch(reward -> reward.getReward_Id().equals(configuration.getName())));
        return true;
    }

    @Override
    public ConditionStatus getStatus() {
        boolean b = DataHandler.getInstance().getPlayerRewardArray(player, subtractNDays(convertTime()), System.currentTimeMillis()).stream().anyMatch(reward -> reward.getReward_Id().equals(config.getName()));
        if (b) {
            return ConditionStatus.OFF;
        } else {
            return ConditionStatus.ON;
        }
    }

    public static long subtractNDays(int nDays) {
        LocalDateTime currentDateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        Duration duration = Duration.ofDays(nDays);
        LocalDateTime subtractedDateTime = currentDateTime.minus(duration);
        return subtractedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
