package cn.ctcraft.ctonlinereward.service.json;

import java.util.Iterator;

public interface JsonArray {
    int size();

    boolean isEmpty();

    void add(Object value);

    Object get(int index);

    String getString(int index);

    int getInt(int index);

    double getDouble(int index);

    boolean getBoolean(int index);

    JsonObject getJsonObject(int index);

    boolean isJsonArray();

    Iterator<JsonObject> getIterator();

    String toJsonString(Boolean format);

    void addFromJsonString(String json);

}
