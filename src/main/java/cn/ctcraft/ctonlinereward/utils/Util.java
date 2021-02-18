package cn.ctcraft.ctonlinereward.utils;

import java.text.SimpleDateFormat;

public class Util {
    public static String getDate(){
        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleDateFormat.format(timeStamp);
    }
}
