package cn.ctcraft.ctonlinereward.service.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.Iterator;

public class GsonJsonArray implements JsonArray {

    private final Gson gson;
    private final com.google.gson.JsonArray jsonArray;

    public GsonJsonArray() {
        gson = new Gson();
        jsonArray = new com.google.gson.JsonArray();
    }

    public GsonJsonArray(com.google.gson.JsonArray jsonArray) {
        gson = new Gson();
        this.jsonArray = jsonArray;
    }

    @Override
    public int size() {
        return jsonArray.size();
    }

    @Override
    public boolean isEmpty() {
        return jsonArray.size() == 0;
    }

    @Override
    public void add(Object value) {
        jsonArray.add(gson.toJsonTree(value));
    }

    @Override
    public void addFromJsonString(String json) {
        jsonArray.add(new JsonParser().parse(json));
    }

    @Override
    public Object get(int index) {
        return jsonArray.get(index);
    }

    @Override
    public String getString(int index) {
        return jsonArray.get(index).getAsString();
    }

    @Override
    public int getInt(int index) {
        return jsonArray.get(index).getAsInt();
    }

    @Override
    public double getDouble(int index) {
        return jsonArray.get(index).getAsDouble();
    }

    @Override
    public boolean getBoolean(int index) {
        return jsonArray.get(index).getAsBoolean();
    }

    @Override
    public JsonObject getJsonObject(int index) {
        return new GsonJsonObject(jsonArray.get(index).getAsJsonObject());
    }

    @Override
    public boolean isJsonArray() {
        return jsonArray.isJsonArray();
    }

    @Override
    public String toJsonString(Boolean format) {
        if (!format) {
            return gson.toJson(jsonArray);
        } else {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(jsonArray);
        }
    }

    @Override
    public Iterator<JsonObject> getIterator() {
        return new GsonIterator(jsonArray.iterator());
    }

    private static class GsonIterator implements Iterator<JsonObject> {

        private final Iterator<JsonElement> iterator;

        private GsonIterator(Iterator<JsonElement> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public JsonObject next() {
            JsonElement jsonElement = iterator.next();
            return new GsonJsonObject(jsonElement.getAsJsonObject());
        }

    }
}
