package cn.ctcraft.ctonlinereward.service;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.pojo.RewardData;
import cn.ctcraft.ctonlinereward.pojo.rewardconditions.RewardCondition;
import cn.ctcraft.ctonlinereward.utils.ClassUtils;
import cn.ctcraft.ctonlinereward.utils.SerializableUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class RewardService {
    private static final RewardService instance = new RewardService();
    private Map<String, ConfigurationSection> rewards = new ConcurrentHashMap<>();
    CtOnlineReward ctOnlineReward;

    private List<Class<? extends RewardCondition>> conditions = new ArrayList<>();

    private RewardService() {
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    }

    public static RewardService getInstance() {
        return instance;
    }

    public Class<? extends RewardCondition> getCondition(String name){
        for (Class<? extends RewardCondition> condition : conditions) {
            if (condition.getSimpleName().equalsIgnoreCase(name)){
                return condition;
            }
        }
        return null;
    }

    public void loadRewardYamlToMemory(Map<String, ConfigurationSection> rewards) {
        this.rewards = rewards;
    }

    public ConfigurationSection getRewardSection(String rewardId) {
        if (rewards.containsKey(rewardId)) {
            return rewards.get(rewardId);
        } else {
            throw new RuntimeException("没有找到id为" + rewardId + "的奖励配置,请检查rewards目录下的配置文件");
        }
    }

    public List<ItemStack> getItemStackFromRewardId(String rewardId) {
        if (!rewards.containsKey(rewardId)) {
            return null;
        }
        ConfigurationSection rewardIdYaml = rewards.get(rewardId);
        if (!rewardIdYaml.contains("rewardData")) {
            return null;
        }
        String rewardDataPath = rewardIdYaml.getString("rewardData");
        File rewardDataFile = new File(ctOnlineReward.getDataFolder(), "rewardData/" + rewardDataPath);
        return getItemStackFromFile(rewardDataFile);
    }

    public List<ItemStack> getItemStackFromFile(File file) {
        Logger logger = ctOnlineReward.getLogger();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bFile = new byte[(int) file.length()];
            fileInputStream.read(bFile);
            SerializableUtil serializableUtil = new SerializableUtil();
            RewardData rewardData = serializableUtil.singleObjectFromByteArray(bFile, RewardData.class);
            fileInputStream.close();
            return rewardData.getRewardList();
        } catch (FileNotFoundException e) {
            String message = e.getMessage();
            if (message.contains("系统找不到指定的文件")) {
                int startIndex = 34;
                int endIndex = message.indexOf("(系统找不到指定的文件。)");
                String rewardDataName = message.substring(startIndex, endIndex);
                logger.warning("§c§l■ 找不到奖励数据!");
                logger.warning("§c§l■ 请使用/cor reward set " + rewardDataName + "设置奖励数据!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("§c§l■ 奖励数据读取失败!");
        }
        return null;
    }


    public boolean saveRewardData(RewardData rewardData, String reward) {
        Logger logger = ctOnlineReward.getLogger();
        try {
            byte[] rewardDataBytes = getRewardDataBytes(rewardData);
            File file = new File(ctOnlineReward.getDataFolder(), "rewardData/" + reward);
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
                if (newFile) {
                    logger.info("§a§l● 奖励数据创建成功，奖励名为" + reward);
                } else {
                    logger.warning("§c§l■ 奖励数据创建失败，奖励名为" + reward);
                }
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(rewardDataBytes);
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("§c§l■ 奖励数据保存失败!");
        }
        return false;
    }

    public byte[] getRewardDataBytes(RewardData rewardData) throws IOException {
        SerializableUtil serializableUtil = new SerializableUtil();
        return serializableUtil.singleObjectToByteArray(rewardData);
    }

    public boolean initRewardFile() {
        ItemStack itemStack = new ItemStack(Material.APPLE);
        RewardData rewardData = new RewardData(Collections.singletonList(itemStack));
        return saveRewardData(rewardData, "10min");
    }

    public void registerRewardCondition() {
        List<Class<?>> subclassesOfAbstractClass = ClassUtils.getSubclassesOfAbstractClass("cn.ctcraft.ctonlinereward.pojo.rewardconditions", RewardCondition.class);
        for (Class<?> ofAbstractClass : subclassesOfAbstractClass) {
            conditions.add((Class<? extends RewardCondition>) ofAbstractClass);
        }
    }


}
