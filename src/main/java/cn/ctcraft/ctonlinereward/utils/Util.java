package cn.ctcraft.ctonlinereward.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Util {
    public static String getDate(){
        return getDate(System.currentTimeMillis());
    }

    public static String getDate(long timeStamp){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleDateFormat.format(timeStamp);
    }

    public static String getVersionString(){
        return Bukkit.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit.","");
    }

    public static List<String> getWeekString() {
        List<String> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        // Set the first day of the week to Sunday
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Calculate the start of the week
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

        for (int i = 1; i <= 7; i++) {
            list.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return list;
    }

    public static List<String> getMonthString(){
        List<String> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        cal.add(Calendar.DAY_OF_MONTH,-dayOfMonth);
        for (int i = 1; i <= dayOfMonth; i++) {
            cal.add(Calendar.DATE,1);
            list.add(sdf.format(cal.getTime()));
        }
        return list;
    }

    public static int getOnlineTimeByJsonObject(JsonObject jsonObject){
        JsonElement time = jsonObject.get("time");
        if (time == null){
            return 0;
        }
        return time.getAsInt();
    }

    public static int timestampToHours(long timestamp){
        SimpleDateFormat hh = new SimpleDateFormat("HH");
        return Integer.parseInt(hh.format(timestamp));
    }
}
