package cn.ctcraft.ctonlinereward;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class LanguageHandler {
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private YamlConfiguration langYaml = CtOnlineReward.lang;
    private String prefix;
    private Boolean debug = ctOnlineReward.getConfig().getBoolean("debug");

    public LanguageHandler() {
        prefix = langYaml.getString("prefix");
    }

    public String getLang(String key) {
        String string = langYaml.getString(key);
        return string.replace("&", "§").replace("{prefix}", prefix);
    }

    public void info(String str, Object... vars) {
        ctOnlineReward.getLogger().info(getValue(str, vars));
    }

    public void debug(String str, Object... vars) {
        if (!debug) {
            return;
        }
        ctOnlineReward.getLogger().info(getValue(str, vars));
    }

    public String getValue(String str, Object... values) {
        Map<String, String> map = new HashMap<String, String>() {{
            for (int i = 0; i < values.length; i += 2) {
                put(values[i].toString(), values[i + 1].toString());
            }
        }};
        return format(str, map);
    }


    public String format(String template, Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            template = template.replace(placeholder, entry.getValue());
        }
        return template;
    }

}
