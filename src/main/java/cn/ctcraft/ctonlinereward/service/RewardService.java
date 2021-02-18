package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.utils.SerializableUtil;
import com.google.common.base.Strings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewardService {
    private static RewardService instance = new RewardService();
    CtOnlineReward ctOnlineReward;

    private RewardService() {
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    }

    public static RewardService getInstance() {
        return instance;
    }

    public List<ItemStack> getItemStackFromRewardId(String rewardId) throws Exception {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        Set<String> rewardYamlKeys = rewardYaml.getKeys(false);
        if(!rewardYamlKeys.contains(rewardId)){
            return null;
        }
        ConfigurationSection rewardIdYaml = rewardYaml.getConfigurationSection(rewardId);
        Set<String> rewardIdYamlKeys = rewardIdYaml.getKeys(false);
        if(!rewardIdYamlKeys.contains("rewardData")){
            return null;
        }
        String rewardData = rewardIdYaml.getString("rewardData");
        File file1 = new File(ctOnlineReward.getDataFolder() + "/rewardData/" + rewardData);
        return getItemStackFromFile(file1);
    }

    public List<ItemStack> getItemStackFromFile(File file){
        Logger logger = ctOnlineReward.getLogger();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            while (br.ready()) {
                sb.append(br.readLine());
            }
            SerializableUtil serializableUtil = new SerializableUtil();
            String s = sb.toString();
            String[] split = s.split("\\(fenge\\)");
            List<ItemStack> list = new ArrayList<>();
            for (String s1 : split) {
                ItemStack itemStack = null;
                if (!Strings.isNullOrEmpty(s1) && !s1.equalsIgnoreCase("null")) {
                    itemStack = serializableUtil.singleObjectFromString(s1, ItemStack.class);
                }
                list.add(itemStack);
            }
            return list;
        }catch (FileNotFoundException e){
            String message = e.getMessage();
            boolean b = message.contains("系统找不到指定的文件");
            if(b){
                int i = message.indexOf("(系统找不到指定的文件。)");
                System.out.println(i);
                String substring = message.substring(34, i);
                ctOnlineReward.getLogger().warning("§c§l■ 找不到奖励数据!");
                ctOnlineReward.getLogger().warning("§c§l■ 请使用/cor reward set "+substring+"设置奖励数据!");
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.warning("§c§l■ 奖励数据读取失败!");
        }
        return null;
    }


    public boolean saveRewardDate(List<ItemStack> stacks, String reward) {
        Logger logger = ctOnlineReward.getLogger();
        try {
            String rewardDate = getRewardDate(stacks);
            File file = new File(ctOnlineReward.getDataFolder() + "/rewardData/" + reward);
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
                if (newFile) {
                    logger.info("§a§l● 奖励数据创建成功,奖励名为" + reward);
                } else {
                    logger.warning("§c§l■ 奖励数据创建失败,奖励名为" + reward);
                }
            }

            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(rewardDate);
            bw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("§c§l■ 奖励数据保存失败!");
        }
        return false;
    }

    public String getRewardDate(List<ItemStack> stacks) throws IOException {
        SerializableUtil serializableUtil = new SerializableUtil();
        String result = "";
        for (ItemStack stack : stacks) {
            String s = serializableUtil.singleObjectToString(stack);
            if (Strings.isNullOrEmpty(result)) {
                result = s;
            } else {
                result = result + "(fenge)" + s;
            }
        }
        return result;
    }

}
