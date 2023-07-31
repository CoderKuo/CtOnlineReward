package cn.ctcraft.ctonlinereward.utils;

import cn.ctcraft.ctonlinereward.service.json.*;

public class JsonUtils {

    public static JsonArray newJsonArray() {
        JsonArray jsonArray = null;
        if (isGsonLoaded()) {
            jsonArray = new GsonJsonArray();
        } else {
            jsonArray = new FastJsonArray();
        }
        return jsonArray;
    }

    public static JsonObject newJsonObject() {
        JsonObject jsonObject = null;
        if (isGsonLoaded()) {
            jsonObject = new GsonJsonObject();
        } else {
            jsonObject = new FastJsonObject();
        }
        return jsonObject;
    }

    public static JsonObject parse(String json) {
        if (isGsonLoaded()) {
            return new GsonJsonObject(json);
        } else {
            return new FastJsonObject(json);
        }
    }

    public static boolean isGsonLoaded() {
        try {
            // 尝试加载Gson类
            Class.forName("com.google.gson.Gson");
            return true;
        } catch (ClassNotFoundException e) {
            // 如果捕获到ClassNotFoundException异常，则表示未加载Gson类
            return false;
        }
    }

}
