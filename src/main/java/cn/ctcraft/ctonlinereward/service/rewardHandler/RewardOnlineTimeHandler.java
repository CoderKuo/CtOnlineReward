package cn.ctcraft.ctonlinereward.service.rewardHandler;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.LanguageHandler;
import cn.ctcraft.ctonlinereward.database.DataService;
import cn.ctcraft.ctonlinereward.pojo.rewardconditions.ConditionStatus;
import cn.ctcraft.ctonlinereward.pojo.rewardconditions.RewardCondition;
import cn.ctcraft.ctonlinereward.pojo.rewardconditions.Stateful;
import cn.ctcraft.ctonlinereward.service.RewardService;
import cn.ctcraft.ctonlinereward.service.RewardStatus;
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

    public boolean timeIsOk = onlineTimeIsOk();

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
            } catch (Exception e) {
                CtOnlineReward.languageHandler.info(key + " condition加载失败");
                e.printStackTrace();
            }
        }

    }

    public RewardStatus getRewardStatus() {
        if (conditionList == null) {
            return RewardStatus.before;
        }
        boolean isOk = onlineTimeIsOk();
        boolean isOn = conditionList.stream().filter(rewardCondition -> Stateful.class.isAssignableFrom(rewardCondition.getClass())).allMatch(rewardCondition -> {
            ConditionStatus status = ((Stateful) rewardCondition).getStatus();
            return status == ConditionStatus.ON;
        });

        if (isOk && isOn) {
            return RewardStatus.activation;
        } else if (!isOn) {
            return RewardStatus.after;
        } else {
            return RewardStatus.before;
        }
    }

    public boolean onlineTimeIsOk() {
        if (conditionList == null) {
            CtOnlineReward.languageHandler.info("{0} 奖励配置中condition不应为空", 0, rewardSection.getName());
            return false;
        }
        return conditionList.stream().allMatch(rewardCondition -> {
            return (rewardCondition.checkFunctions(rewardSection) && rewardCondition.check());
        });
    }

    /**
     * 解析PAPI
     *
     * @param placeholder 传入的PAPI占位符 示例传入值: condition_placeholderName
     * @return 解析后的文本
     */
    public String getPlaceholder(String placeholder) {
        String[] s = placeholder.split("_");
        if (s.length != 2) {
            throw new RuntimeException("错误的PAPI变量,请检查PAPI变量格式是否为: %cor_rewardId_condition_placeholderName%");
        }
        return conditionList.stream().filter(rewardCondition -> rewardCondition.getName().equalsIgnoreCase(s[0])).findFirst().map(rewardCondition -> rewardCondition.placeholder(s[1])).get();
    }


}
