package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.pojo.RewardData;
import cn.ctcraft.ctonlinereward.utils.SerializableUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class RewardService {
    private static RewardService instance = new RewardService();
    CtOnlineReward ctOnlineReward;

    private RewardService() {
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    }

    public static RewardService getInstance() {
        return instance;
    }

    public List<ItemStack> getItemStackFromRewardId(String rewardId) {
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
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bFile = new byte[(int) file.length()];
            fileInputStream.read(bFile);
            SerializableUtil serializableUtil = new SerializableUtil();
            RewardData rewardData = serializableUtil.singleObjectFromByteArray(bFile, RewardData.class);
            return rewardData.getRewardList();
        }catch (FileNotFoundException e){
            String message = e.getMessage();
            boolean b = message.contains("系统找不到指定的文件");
            if(b){
                int i = message.indexOf("(系统找不到指定的文件。)");
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


    public boolean saveRewardDate(RewardData rewardData, String reward) {
        Logger logger = ctOnlineReward.getLogger();
        try {
            byte[] rewardDate = getRewardDate(rewardData);
            File file = new File(ctOnlineReward.getDataFolder() + "/rewardData/" + reward);
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
                if (newFile) {
                    logger.info("§a§l● 奖励数据创建成功,奖励名为" + reward);
                } else {
                    logger.warning("§c§l■ 奖励数据创建失败,奖励名为" + reward);
                }
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(rewardDate);
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("§c§l■ 奖励数据保存失败!");
        }
        return false;
    }

    public byte[] getRewardDate(RewardData rewardData) throws IOException {
        SerializableUtil serializableUtil = new SerializableUtil();
        return serializableUtil.singleObjectToByteArray(rewardData);

    }

}
