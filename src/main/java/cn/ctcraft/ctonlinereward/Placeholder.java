package cn.ctcraft.ctonlinereward;

import cn.ctcraft.ctonlinereward.database.DataService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.udojava.evalex.Expression;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Placeholder extends PlaceholderExpansion {
    private final CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private final DataService playerDataService = CtOnlineReward.dataService;
    private JsonObject papijson = new JsonObject();

    public Placeholder() {
        YamlConfiguration placeholderYaml = CtOnlineReward.placeholder;
        Set<String> keys = placeholderYaml.getKeys(false);
        for (String key : keys) {
            ConfigurationSection configurationSection = placeholderYaml.getConfigurationSection(key);
            String text = configurationSection.getString("text");
            ConfigurationSection value = configurationSection.getConfigurationSection("value");
            String type = value.getString("type");
            boolean hasFormula = value.getKeys(false).contains("formula");
            if (hasFormula) {
                String formula = value.getString("formula");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", type);
                jsonObject.addProperty("formula", formula);
                papijson.add(text, jsonObject);
            } else {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", type);
                papijson.add(text, jsonObject);
            }
        }
        ctOnlineReward.getLogger().info(ChatColor.GREEN + "成功加载" + papijson.size() + "个papi变量");
    }


    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        switch (params) {
            case "player":
                return player.getName();
            case "world":
                return player.isOnline() ? ((Player)player).getWorld().getName() : "玩家不在线";
            case "onlinetime":
                return String.valueOf(playerDataService.getPlayerOnlineTime(player));
            case "weekonlinetime":
                return String.valueOf(playerDataService.getPlayerOnlineTimeWeek(player));
            case "monthonlinetime":
                return String.valueOf(playerDataService.getPlayerOnlineTimeMonth(player));
            case "allonlinetime":
                return String.valueOf(playerDataService.getPlayerOnlineTimeAll(player));
        }

        boolean has = papijson.has(params);
        if (has) {
            JsonElement jsonElement = papijson.get(params);
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            String type = asJsonObject.get("type").getAsString();boolean hasFormula = asJsonObject.has("formula");
            if (hasFormula) {
                ScriptEngine javaScript = new ScriptEngineManager().getEngineByName("JavaScript");
                String formula = asJsonObject.get("formula").getAsString();
                String newFormula;
                switch (type) {
                    case "all":
                        newFormula = formula.replace("x", String.valueOf(playerDataService.getPlayerOnlineTimeAll(player)));
                        break;
                    case "week":
                        newFormula = formula.replace("x", String.valueOf(playerDataService.getPlayerOnlineTimeWeek(player)));
                        break;
                    case "month":
                        newFormula = formula.replace("x", String.valueOf(playerDataService.getPlayerOnlineTimeMonth(player)));
                        break;
                    case "day":
                        newFormula = formula.replace("x", String.valueOf(playerDataService.getPlayerOnlineTime(player)));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + type);
                }
                try {
                    if (javaScript == null){
                        Expression expression = new Expression(newFormula);
                        BigDecimal eval = expression.eval();
                        return String.format("%.2f",eval);
                    }
                    Object eval = javaScript.eval(newFormula);
                    if (eval instanceof Double){
                        return String.format("%.2f", eval);
                    }
                    return String.valueOf(eval);
                } catch (ScriptException e) {
                    ctOnlineReward.getLogger().warning("§c§l■ papi变量公式错误,请检查公式格式是否正确!");
                    e.printStackTrace();
                }
            } else {
                switch (type) {
                    case "all":
                        return String.valueOf(playerDataService.getPlayerOnlineTimeAll(player));
                    case "week":
                        return String.valueOf(playerDataService.getPlayerOnlineTimeWeek(player));
                    case "month":
                        return String.valueOf(playerDataService.getPlayerOnlineTimeMonth(player));
                    case "day":
                        return String.valueOf(playerDataService.getPlayerOnlineTime(player));
                }
            }
        }


        return null;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }


        switch (params) {
            case "player":
                return player.getName();
            case "world":
                return player.getWorld().getName();
            case "onlineTime":
                return String.valueOf(playerDataService.getPlayerOnlineTime(player));
            case "weekOnlineTime":
                return String.valueOf(playerDataService.getPlayerOnlineTimeWeek(player));
            case "monthOnlineTime":
                return String.valueOf(playerDataService.getPlayerOnlineTimeMonth(player));
            case "allOnlineTime":
                return String.valueOf(playerDataService.getPlayerOnlineTimeAll(player));
        }

        boolean has = papijson.has(params);
        if (has) {
            JsonElement jsonElement = papijson.get(params);
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            String type = asJsonObject.get("type").getAsString();boolean hasFormula = asJsonObject.has("formula");
            if (hasFormula) {
                ScriptEngine javaScript = new ScriptEngineManager().getEngineByName("JavaScript");
                String formula = asJsonObject.get("formula").getAsString();
                String newFormula;
                switch (type) {
                    case "all":
                        newFormula = formula.replace("x", String.valueOf(playerDataService.getPlayerOnlineTimeAll(player)));
                        break;
                    case "week":
                        newFormula = formula.replace("x", String.valueOf(playerDataService.getPlayerOnlineTimeWeek(player)));
                        break;
                    case "month":
                        newFormula = formula.replace("x", String.valueOf(playerDataService.getPlayerOnlineTimeMonth(player)));
                        break;
                    case "day":
                        newFormula = formula.replace("x", String.valueOf(playerDataService.getPlayerOnlineTime(player)));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + type);
                }
                try {
                    if (javaScript == null){
                        Expression expression = new Expression(newFormula);
                        BigDecimal eval = expression.eval();
                        return String.format("%.2f",eval);
                    }
                    Object eval = javaScript.eval(newFormula);
                    if (eval instanceof Double){
                        return String.format("%.2f", eval);
                    }
                    return String.valueOf(eval);
                } catch (ScriptException e) {
                    ctOnlineReward.getLogger().warning("§c§l■ papi变量公式错误,请检查公式格式是否正确!");
                    e.printStackTrace();
                }
            } else {
                switch (type) {
                    case "all":
                        return String.valueOf(playerDataService.getPlayerOnlineTimeAll(player));
                    case "week":
                        return String.valueOf(playerDataService.getPlayerOnlineTimeWeek(player));
                    case "month":
                        return String.valueOf(playerDataService.getPlayerOnlineTimeMonth(player));
                    case "day":
                        return String.valueOf(playerDataService.getPlayerOnlineTime(player));
                }
            }
        }


        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "CtOnlineReward";
    }

    @Override
    public @NotNull String getAuthor() {
        return "大阔";
    }

    @Override
    public @NotNull String getVersion() {
        return ctOnlineReward.getDescription().getVersion();
    }
}
