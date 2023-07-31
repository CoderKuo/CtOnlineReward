package cn.ctcraft.ctonlinereward.service.json;

public interface JsonObject {
    void put(String key, Object value);

    String getString(String key);

    int getInt(String key);

    double getDouble(String key);

    boolean getBoolean(String key);

    JsonObject getJsonObject(String key);

    int size();

    boolean has(String member);

    boolean isJsonNull();

    JsonArray getJsonArray(String member);

    String toJsonString(Boolean format);

}
