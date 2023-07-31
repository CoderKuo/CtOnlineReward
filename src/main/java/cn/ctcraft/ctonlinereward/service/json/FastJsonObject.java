package cn.ctcraft.ctonlinereward.service.json;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public class FastJsonObject implements JsonObject {


    private final JSONObject jsonObject;

    public FastJsonObject() {
        jsonObject = new JSONObject();
    }

    public FastJsonObject(String json) {
        jsonObject = JSONObject.parseObject(json);
    }

    public FastJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public void put(String key, Object value) {
        jsonObject.put(key, value);
    }

    @Override
    public String getString(String key) {
        return jsonObject.getString(key);
    }

    @Override
    public int getInt(String key) {
        return jsonObject.getIntValue(key);
    }

    @Override
    public double getDouble(String key) {
        return jsonObject.getDoubleValue(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return jsonObject.getBooleanValue(key);
    }

    @Override
    public JsonObject getJsonObject(String key) {
        return new FastJsonObject(jsonObject.getJSONObject(key));
    }

    @Override
    public int size() {
        return jsonObject.size();
    }

    @Override
    public boolean has(String member) {
        return jsonObject.containsKey(member);
    }

    @Override
    public boolean isJsonNull() {
        return jsonObject.isEmpty();
    }

    @Override
    public String toJsonString(Boolean format) {
        if (!format) {
            return jsonObject.toString();
        } else {
            return jsonObject.toString(JSONWriter.Feature.PrettyFormat);
        }
    }

    @Override
    public JsonArray getJsonArray(String member) {
        return new FastJsonArray(jsonObject.getJSONArray(member));
    }
}
