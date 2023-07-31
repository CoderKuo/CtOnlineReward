package cn.ctcraft.ctonlinereward.service.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GsonJsonObject implements JsonObject {

    private final Gson gson;
    private final com.google.gson.JsonObject jsonObject;

    public GsonJsonObject() {
        gson = new Gson();
        jsonObject = new com.google.gson.JsonObject();
    }

    public GsonJsonObject(String json) {
        this.gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        this.jsonObject = jsonParser.parse(json).getAsJsonObject();
    }

    public GsonJsonObject(com.google.gson.JsonObject jsonObject) {
        this.gson = new Gson();
        this.jsonObject = jsonObject;
    }

    @Override
    public void put(String key, Object value) {
        JsonElement jsonElement = gson.toJsonTree(value);
        jsonObject.add(key, jsonElement);
    }

    @Override
    public String getString(String key) {
        return jsonObject.get(key).getAsString();
    }

    @Override
    public int getInt(String key) {
        return jsonObject.get(key).getAsInt();
    }

    @Override
    public double getDouble(String key) {
        return jsonObject.get(key).getAsDouble();
    }

    @Override
    public boolean getBoolean(String key) {
        return jsonObject.get(key).getAsBoolean();
    }

    @Override
    public JsonObject getJsonObject(String key) {
        return new GsonJsonObject(jsonObject.getAsJsonObject(key));
    }

    @Override
    public int size() {
        return jsonObject.size();
    }

    @Override
    public boolean has(String member) {
        return jsonObject.has(member);
    }

    @Override
    public boolean isJsonNull() {
        return jsonObject.isJsonNull();
    }

    @Override
    public String toJsonString(Boolean format) {
        if (!format) {
            return gson.toJson(jsonObject);
        } else {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(jsonObject);
        }
    }

    @Override
    public JsonArray getJsonArray(String member) {
        return new GsonJsonArray(jsonObject.getAsJsonArray(member));
    }
}
