package cn.ctcraft.ctonlinereward.inventory;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.RewardEntity;
import cn.ctcraft.ctonlinereward.database.DataService;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.service.RewardStatus;
import cn.ctcraft.ctonlinereward.service.YamlService;
import cn.ctcraft.ctonlinereward.service.rewardHandler.RewardOnlineTimeHandler;
import cn.ctcraft.ctonlinereward.utils.Util;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class InventoryFactory {
    private Player player;
    private Map<ItemStack,RewardEntity> map = new HashMap<>();
    private MainInventoryHolder mainInventoryHolder = new MainInventoryHolder();
    private YamlService yamlService = YamlService.getInstance();
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    public static Inventory build(String inventoryId, Player player) {
        return new InventoryFactory().getInventory(inventoryId, player);
    }

    private Inventory getInventory(String inventoryId, Player player) {
        this.player = player;
        Map<String, YamlConfiguration> guiYaml = YamlData.guiYaml;
        if(!guiYaml.containsKey(inventoryId)){
            player.sendMessage("§c§l菜单不存在!");
            player.closeInventory();
        }
        YamlConfiguration yamlConfiguration = guiYaml.get(inventoryId);
        String name = yamlConfiguration.getString("name");
        int size = yamlConfiguration.getInt("slot");
        Inventory inventory = Bukkit.createInventory(mainInventoryHolder, size, name.replace("&", "§"));
        addItemStack(inventory, yamlConfiguration);
        mainInventoryHolder.inventoryID = inventoryId;
        return inventory;
    }

    private void addItemStack(Inventory inventory, YamlConfiguration guiYaml) {
        ConfigurationSection values = guiYaml.getConfigurationSection("values");
        Set<String> keys = values.getKeys(false);
        for (String key : keys) {
            ConfigurationSection value = values.getConfigurationSection(key);
            ItemStack valueItemStack = getValueItemStack(value);
            Set<String> keys1 = value.getKeys(false);

            if(keys1.contains("index")){
                int index = value.getInt("index");
                inventory.setItem(index,valueItemStack);
                if(keys1.contains("mode")){
                    String mode = value.getString("mode");
                    Map<Integer, String> modeMap = mainInventoryHolder.modeMap;
                    modeMap.put(index,mode);
                    if(mode.equalsIgnoreCase("reward")){
                        RewardEntity rewardEntity = map.get(valueItemStack);
                        Map<Integer, RewardEntity> statusMap = mainInventoryHolder.statusMap;
                        statusMap.put(index,rewardEntity);
                    }
                    if(mode.equalsIgnoreCase("command")){
                        ConfigurationSection configurationSection = getItemStackCommand(value);
                        mainInventoryHolder.commandMap.put(index,configurationSection);
                    }
                    if(mode.equalsIgnoreCase("gui")){
                        if(keys1.contains("gui")){
                            mainInventoryHolder.guiMap.put(index,value.getString("gui"));
                        }
                    }
                }

            }

        }
    }

    private ConfigurationSection getItemStackCommand(ConfigurationSection value){
        Set<String> keys = value.getKeys(false);
        if(!keys.contains("command")){
            return null;
        }
        return value.getConfigurationSection("command");
    }

    private ItemStack getValueItemStack(ConfigurationSection value) {
        Set<String> keys = value.getKeys(false);
        ItemStack itemStack = getItemStackType(null, value);
        ItemMeta itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMetaHandler(value,itemMeta);
        itemStack.setItemMeta(itemMeta);
        if(!keys.contains("mode")){
            return itemStack;
        }
        String mode = value.getString("mode");
        if(mode.equalsIgnoreCase("reward")){
            if(keys.contains("rewardId")){
                extendHandler(itemStack,value,value.getString("rewardId"));
            }
        }

        return itemStack;
    }


    private void extendHandler(ItemStack itemStack,ConfigurationSection value,String rewardId) {
        Set<String> keys = value.getKeys(false);
        if(!keys.contains("extend")){
            return;
        }
        RewardStatus rewardStatus = getRewardStatus(player, rewardId);
        RewardEntity rewardEntity = new RewardEntity(rewardId, rewardStatus);
        ConfigurationSection extend = value.getConfigurationSection("extend");

        switch (rewardStatus){
            case before:
                ConfigurationSection before = extend.getConfigurationSection("before");
                itemStack = getItemStackType(itemStack, before);
                ItemMeta itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
                itemMetaHandler(before, itemMeta);
                itemStack.setItemMeta(itemMeta);
                break;
            case after:
                ConfigurationSection after = extend.getConfigurationSection("after");
                itemStack = getItemStackType(itemStack, after);
                ItemMeta itemMeta2 = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
                itemMetaHandler(after, itemMeta2);
                itemStack.setItemMeta(itemMeta2);
                break;
            case activation:
                ConfigurationSection activation = extend.getConfigurationSection("activation");
                itemStack = getItemStackType(itemStack, activation);
                ItemMeta itemMeta3 = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
                itemMetaHandler(activation, itemMeta3);
                itemStack.setItemMeta(itemMeta3);
        }
        map.put(itemStack,rewardEntity);
    }

    private ItemStack getItemStackType(ItemStack itemStack,ConfigurationSection config){

        Set<String> keys = config.getKeys(false);
        if(!keys.contains("type")){
            CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
            config = plugin.getConfig().getConfigurationSection("Setting.defaultItemType");
        }
        ConfigurationSection type = config.getConfigurationSection("type");
        Set<String> typeKeys = type.getKeys(false);
        if(typeKeys.contains("name")){
            String name = type.getString("name");
            if(itemStack == null){
                itemStack = new ItemStack(getItemStackByNMS(name));
            }else {
                itemStack.setType(getItemStackByNMS(name).getType());
            }
        }
        if(itemStack == null){
            itemStack = new ItemStack(Material.CHEST);
        }
        if (itemStack.getType() == Material.AIR){
            itemStack = new ItemStack(Material.CHEST);
        }
        if(typeKeys.contains("enchantment")){
            boolean enchantment = type.getBoolean("enchantment");
            if(enchantment){
                itemStack.addUnsafeEnchantment(Enchantment.DURABILITY,1);
            }else{
                itemStack.removeEnchantment(Enchantment.DURABILITY);
            }
        }
        return itemStack;
    }

    private ItemStack getItemStackByNMS(String name){
        if (isInteger(name)){
            return new ItemStack(Material.getMaterial(Integer.parseInt(name)));
        }

        String[] split = name.split(":");
        if (split[0].equalsIgnoreCase("minecraft")){
            Material material = Material.getMaterial(split[1].toUpperCase());
            if (material != null){
                return new ItemStack(material);
            }
        }
        String versionString = Util.getVersionString();
        try {
            Class<?> itemClass = Class.forName("net.minecraft.server." + versionString + ".Item");
            Method b = itemClass.getMethod("b", String.class);
            Object invoke = b.invoke(itemClass, name);
            Class<?> itemStackClass = Class.forName("net.minecraft.server." + versionString + ".ItemStack");
            Constructor<?> itemStackConstructor = itemStackClass.getDeclaredConstructor(itemClass);
            Object nmsItemStack = itemStackConstructor.newInstance(invoke);
            Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit." + versionString + ".inventory.CraftItemStack");
            Method asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", itemStackClass);
            return (ItemStack) asBukkitCopy.invoke(craftItemStack, nmsItemStack);
        }catch (Exception e){
            throw new RuntimeException("GUI物品材质名称配置错误!",e);
        }
    }

    private static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    private void itemMetaHandler(ConfigurationSection config,ItemMeta itemMeta){
        Set<String> configKeys = config.getKeys(false);
        if(configKeys.contains("name")){
            String name = config.getString("name").replace("&", "§");
            String s = PlaceholderAPI.setPlaceholders(player, name);
            itemMeta.setDisplayName(s);
        }
        if(configKeys.contains("lore")){
            List<String> lore = config.getStringList("lore");
            lore.replaceAll(a->a.replace("&","§"));
            List<String> list = PlaceholderAPI.setPlaceholders(player, lore);
            itemMeta.setLore(list);
        }
    }

    private RewardStatus getRewardStatus(Player player,String rewardId){
        ConfigurationSection configurationSection = YamlData.rewardYaml.getConfigurationSection(rewardId);
        if (configurationSection == null){
            ctOnlineReward.getLogger().warning("§c§l■ 未找到奖励配置 §f§n" + rewardId+"§c§l 请检查reward.yml配置文件中是否有指定配置!");
            return RewardStatus.before;
        }
        Set<String> keys = configurationSection.getKeys(false);
        if(!keys.contains("time")){
            return RewardStatus.before;
        }
        boolean timeIsOk = RewardOnlineTimeHandler.getInstance().onlineTimeIsOk(player, configurationSection.getString("time"));
        if(!timeIsOk) {
            return RewardStatus.before;
        }
        List<String> playerRewardArray = CtOnlineReward.dataService.getPlayerRewardArray(player);
        if(playerRewardArray.size() == 0){
            return RewardStatus.activation;
        }
        if(!playerRewardArray.contains(rewardId)){
            return RewardStatus.activation;
        }
        return RewardStatus.after;

    }


}
