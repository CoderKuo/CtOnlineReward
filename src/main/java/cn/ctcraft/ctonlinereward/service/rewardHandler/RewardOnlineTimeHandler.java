package cn.ctcraft.ctonlinereward.service.rewardHandler;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.DataService;
import com.udojava.evalex.Expression;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;

public class RewardOnlineTimeHandler {
    private DataService dataService = CtOnlineReward.dataService;
    private static RewardOnlineTimeHandler instance = new RewardOnlineTimeHandler();
    private RewardOnlineTimeHandler(){}

    public static RewardOnlineTimeHandler getInstance() {
        return instance;
    }
    
    public boolean onlineTimeIsOk(Player player,String timeFormula){
        String temp = variablesHandler(player, timeFormula);
        ScriptEngine javaScript = new ScriptEngineManager().getEngineByName("JavaScript");
        if (javaScript == null){
            Expression expression = new Expression(temp);
            BigDecimal eval = expression.eval();
            if (eval.intValue() == 1){
                return true;
            }else{
                return false;
            }
        }
        try {
            return (boolean) javaScript.eval(temp);
        } catch (ScriptException e) {
            throw new RuntimeException("请检查计算公式是否为正确",e);
        } catch (ClassCastException e){
            throw new RuntimeException("请检查计算公式是否为完整等式",e);
        }
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
