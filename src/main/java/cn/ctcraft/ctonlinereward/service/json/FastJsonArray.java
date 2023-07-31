package cn.ctcraft.ctonlinereward.service.json;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

import java.util.Iterator;

public class FastJsonArray implements JsonArray {


    private final JSONArray jsonArray;

    public FastJsonArray() {
        jsonArray = new JSONArray();
    }

    public FastJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public int size() {
        return jsonArray.size();
    }

    @Override
    public boolean isEmpty() {
        return jsonArray.isEmpty();
    }

    @Override
    public void add(Object value) {
        jsonArray.add(JSONObject.parse(value.toString()));
    }

    @Override
    public void addFromJsonString(String json) {
        jsonArray.add(JSONObject.parse(json));
    }

    @Override
    public Object get(int index) {
        return jsonArray.get(index);
    }

    @Override
    public String getString(int index) {
        return jsonArray.getString(index);
    }

    @Override
    public int getInt(int index) {
        return jsonArray.getIntValue(index);
    }

    @Override
    public double getDouble(int index) {
        return jsonArray.getDoubleValue(index);
    }

    @Override
    public boolean getBoolean(int index) {
        return jsonArray.getBooleanValue(index);
    }

    @Override
    public JsonObject getJsonObject(int index) {
        return new FastJsonObject(jsonArray.getJSONObject(index));
    }

    @Override
    public boolean isJsonArray() {
        return jsonArray instanceof JSONArray;
    }

    @Override
    public String toJsonString(Boolean format) {
        if (!format) {
            return jsonArray.toString();
        } else {
            return jsonArray.toString(JSONWriter.Feature.PrettyFormat);
        }
    }

    @Override
    public Iterator<JsonObject> getIterator() {
        return new FastJsonIterator(jsonArray.iterator());
    }

    private static class FastJsonIterator implements Iterator<JsonObject> {
        private final Iterator<Object> iterator;

        private FastJsonIterator(Iterator<Object> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public JsonObject next() {
            Object next = iterator.next();
            return new FastJsonObject((JSONObject) next);
        }
    }
}
