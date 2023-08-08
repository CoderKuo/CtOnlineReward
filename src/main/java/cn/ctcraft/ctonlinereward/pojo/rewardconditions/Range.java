package cn.ctcraft.ctonlinereward.pojo.rewardconditions;

import cn.ctcraft.ctonlinereward.database.DataHandler;
import cn.ctcraft.ctonlinereward.service.json.JsonObject;
import cn.ctcraft.ctonlinereward.utils.JsonUtils;
import jdk.internal.net.http.common.Pair;
import org.bukkit.OfflinePlayer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * 时间范围
 * 传入param需为json字符串
 * 例如:
 * {
 * start: '06:00'
 * end: '19:00'
 * target: 1h
 * }
 * 或指定日期
 * {
 * start: '2023-07-30 06:00'
 * end: '2023-08-01 06:00'
 * target: 1d
 * }
 */
public class Range extends RewardCondition {

    public Range(OfflinePlayer player, String param) {
        super(player, param);
        addPlaceholder("reminder", player1 -> {
            Pair<Long, Long> startAndEndTime = getStartAndEndTime();
            return String.valueOf(DataHandler.getInstance().getPlayerOnlineTimeFromRange(player, startAndEndTime.first, startAndEndTime.second) - convertTime(JsonUtils.parse(param).getString("target")));
        });
    }


    @Override
    public String getName() {
        return "range";
    }

    @Override
    public boolean isNeedConfig() {
        return false;
    }

    @Override
    public boolean check() {
        Pair<Long, Long> startAndEndTime = getStartAndEndTime();
        return DataHandler.getInstance().getPlayerOnlineTimeFromRange(player, startAndEndTime.first, startAndEndTime.second) >= convertTime(JsonUtils.parse(param).getString("target"));
    }

    private Pair<Long, Long> getStartAndEndTime() {
        JsonObject parse = JsonUtils.parse(param);
        String start = parse.getString("start");
        String end = parse.getString("end");

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate today = LocalDate.now();
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (start.contains(" ")) {
            startDateTime = LocalDateTime.parse(start, formatter1);
            endDateTime = LocalDateTime.parse(end, formatter1);
        } else {
            LocalTime startTime = LocalTime.parse(start, formatter2);
            LocalTime endTime = LocalTime.parse(end, formatter2);
            startDateTime = LocalDateTime.of(today, startTime);
            endDateTime = LocalDateTime.of(today, endTime);
        }

        long startTimestamp = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new Pair<>(startTimestamp, endTimestamp);
    }
}
