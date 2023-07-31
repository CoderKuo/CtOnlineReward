package cn.ctcraft.ctonlinereward.pojo.rewardconditions;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class RewardCondition {

    public OfflinePlayer player;
    public String param;
    private List<Function<Configuration, Boolean>> otherCheckFunctions = new ArrayList<>();
    private Map<String, Function<OfflinePlayer, String>> placeholders = new ConcurrentHashMap<>();

    public RewardCondition(OfflinePlayer player, String param) {
        this.player = player;
        this.param = param;
    }

    protected int convertTime(String str) {
        int totalMinutes = 0;
        String[] parts = str.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        for (int i = 0; i < parts.length; i += 2) {
            int value = Integer.parseInt(parts[i]);
            String unit = parts[i + 1].toLowerCase();

            switch (unit) {
                case "d":
                    totalMinutes += value * 24 * 60;
                    break;
                case "h":
                    totalMinutes += value * 60;
                    break;
                case "m":
                    totalMinutes += value;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid time unit: " + unit);
            }
        }
        return totalMinutes;
    }

    protected int convertTime() {
        return convertTime(param);
    }

    public void addFunction(Function<Configuration, Boolean> function) {
        this.otherCheckFunctions.add(function);
    }

    public boolean checkFunctions(Configuration configuration) {
        return otherCheckFunctions.stream().allMatch(function -> function.apply(configuration));
    }

    public String placeholder(String key) {
        return placeholders.get(key).apply(player);
    }

    protected void addPlaceholder(String key, Function<OfflinePlayer, String> function) {
        placeholders.put(key, function);
    }

    abstract public String getName();

    abstract boolean check();
}
