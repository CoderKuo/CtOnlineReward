package cn.ctcraft.ctonlinereward.service.rewardHandler;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.DataService;
import cn.ctcraft.ctonlinereward.pojo.rewardconditions.RewardCondition;
import cn.ctcraft.ctonlinereward.service.RewardService;
import com.udojava.evalex.Expression;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RewardOnlineTimeHandler {

    private Player player;
    private ConfigurationSection rewardSection;
    private ConfigurationSection conditionSection;

    private boolean timeIsOk;

    private List<RewardCondition> conditionList = new ArrayList<>();

    public RewardOnlineTimeHandler(Player player,ConfigurationSection rewardSection){
        this.player = player;
        this.rewardSection = rewardSection;
        this.conditionSection = rewardSection.getConfigurationSection("condition");

        for (String key : conditionSection.getKeys(false)) {
            try {
                Class<? extends RewardCondition> condition = RewardService.getInstance().getCondition(key);
                Constructor<? extends RewardCondition> constructor = condition.getConstructor(OfflinePlayer.class, String.class);
                RewardCondition rewardCondition = constructor.newInstance(player, conditionSection.getString(key));
                if (rewardCondition.isNeedConfig()){
                    rewardCondition.setConfig(rewardSection);
                }
                conditionList.add(rewardCondition);
            }catch (Exception e) {
                /**
                 * 友好报错 暂未完成
                 */
                e.printStackTrace();
            }
        }

    }

    public boolean onlineTimeIsOk() {
        conditionList.stream().anyMatch(rewardCondition -> {
        })
    }

    private String variablesHandler(Player player,String Formula){
        String temp = Formula;
        boolean hasOnlineTime = temp.contains("{onlineTime}");
        if (hasOnlineTime){
            int playerOnlineTime = dataService.getPlayerOnlineTime(player);
            temp = temp.replace("{onlineTime}", String.valueOf(playerOnlineTime));
        }
        boolean hasWeekOnlineTime = temp.contains("{weekOnlineTime}");
        if (hasWeekOnlineTime){
            int playerOnlineTimeWeek = dataService.getPlayerOnlineTimeWeek(player);
            temp = temp.replace("{weekOnlineTime}",String.valueOf(playerOnlineTimeWeek));
        }
        boolean hasMonthOnlineTime = temp.contains("{monthOnlineTime}");
        if (hasMonthOnlineTime){
            int playerOnlineTimeMonth = dataService.getPlayerOnlineTimeMonth(player);
            temp = temp.replace("{monthOnlineTime}",String.valueOf(playerOnlineTimeMonth));
        }
        boolean hasAllOnlineTime = temp.contains("{allOnlineTime}");
        if (hasAllOnlineTime){
            int playerOnlineTimeAll = dataService.getPlayerOnlineTimeAll(player);
            temp = temp.replace("{allOnlineTime}",String.valueOf(playerOnlineTimeAll));
        }
        return temp;
    }
}
