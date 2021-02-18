package cn.ctcraft.ctonlinereward;

import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageHandler {
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private YamlConfiguration langYaml =  CtOnlineReward.lang;
    private String prefix;

    public LanguageHandler(){
        prefix = langYaml.getString("prefix");
    }

    public String getLang(String key){
        String string = langYaml.getString(key);
        return string.replace("&", "§").replace("{prefix}", prefix);
    }
}
