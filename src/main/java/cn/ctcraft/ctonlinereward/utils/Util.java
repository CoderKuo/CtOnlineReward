package cn.ctcraft.ctonlinereward.utils;

import cn.ctcraft.ctonlinereward.service.json.JsonObject;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Util {


    public static String getDate() {
        return getDate(System.currentTimeMillis());
    }

    public static String getDateNew() {
        return getDateNew(System.currentTimeMillis());
    }

    public static String getDateNew(long timeStamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(timeStamp);
    }

    public static String timeDiff(long loginTimeStr, long logoutTimeStr) {
        return timeDiff(getDateNew(loginTimeStr), getDateNew(logoutTimeStr));
    }

    public static String timeDiff(String loginTimeStr, String logoutTimeStr) {
        LocalDateTime loginTime = LocalDateTime.parse(loginTimeStr.replace(" ", "T"));
        LocalDateTime logoutTime = LocalDateTime.parse(logoutTimeStr.replace(" ", "T"));

        return String.valueOf(calculateTimeDifferenceInMinutes(loginTime, logoutTime));
    }

    private static long calculateTimeDifferenceInMinutes(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.toMinutes();
    }


    public static String getDate(long timeStamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleDateFormat.format(timeStamp);
    }

    public static String getVersionString() {
        return Bukkit.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit.", "");
    }

    public static long getStartOfCurrentWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return startOfWeek.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static long getEndOfCurrentWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfWeek = now.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return endOfWeek.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static long getStartOfToday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        return startOfDay.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static long getEndOfToday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.with(LocalTime.MAX);
        return endOfDay.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static long getStartOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDateTime startOfDay = startOfMonth.atStartOfDay();
        return startOfDay.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static long getEndOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        LocalDateTime endOfDay = endOfMonth.atTime(LocalTime.MAX);
        return endOfDay.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static boolean isSameDay(long timestamp1, long timestamp2) {
        LocalDate date1 = Instant.ofEpochMilli(timestamp1).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate date2 = Instant.ofEpochMilli(timestamp2).atZone(ZoneId.systemDefault()).toLocalDate();
        return date1.isEqual(date2);
    }

    public static long calculateMinutesSinceMidnight(long timestamp) {
        LocalDateTime currentTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime todayMidnight = LocalDateTime.of(currentTime.toLocalDate(), LocalTime.MIDNIGHT);
        Duration duration = Duration.between(todayMidnight, currentTime);
        return duration.toMinutes();
    }

    public static int timestampToHours(long timestamp) {
        SimpleDateFormat hh = new SimpleDateFormat("HH");
        return Integer.parseInt(hh.format(timestamp));
    }

    public static Triple<Integer, Integer, Integer> formatMinutesToDaysHoursMinutes(int minutes) {
        MutableTriple<Integer, Integer, Integer> date = new MutableTriple<>(0, 0, 0);
        if (minutes < 0) {
            return date;
        }

        date.left = minutes / (24 * 60);
        int remainingMinutes = minutes % (24 * 60);
        date.middle = remainingMinutes / 60;
        date.right = remainingMinutes % 60;
        return date;
    }
}
