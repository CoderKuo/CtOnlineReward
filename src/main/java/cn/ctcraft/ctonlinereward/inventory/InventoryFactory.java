package cn.ctcraft.ctonlinereward.inventory;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.RewardEntity;
import cn.ctcraft.ctonlinereward.database.DataService;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.service.RewardStatus;
import cn.ctcraft.ctonlinereward.service.YamlService;
import cn.ctcraft.ctonlinereward.service.rewardHandler.RewardOnlineTimeHandler;
import cn.ctcraft.ctonlinereward.utils.ItemUtils;
import cn.ctcraft.ctonlinereward.utils.Position;
import cn.ctcraft.ctonlinereward.utils.Util;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class InventoryFactory {
    private Player player;
    private final Set<RewardEntity> rewardSet = new LinkedHashSet<>();
    private final MainInventoryHolder mainInventoryHolder = new MainInventoryHolder();
    private final YamlService yamlService = YamlService.getInstance();
    private final CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    public static Inventory build(String inventoryId, Player player) {
        return new InventoryFactory().getInventory(inventoryId, player);
    }

    /**
     * 抛弃正则匹配写法，按理说这样更高效
     *
     * @param str 字符串
     * @return 是否是数字
     */
    private static boolean isInteger(String str) {
        int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            if (i == 0 && (ch == '-' || ch == '+')) {
                continue;
            }
            if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    private Inventory getInventory(String inventoryId, Player player) {
        this.player = player;
        Map<String, YamlConfiguration> guiYaml = YamlData.guiYaml;

        if (!guiYaml.containsKey(inventoryId)) {
            player.sendMessage("§c§l菜单不存在!");
            player.closeInventory();
            return null;
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
        if (values == null) {
            return;
        }

        Set<String> keys = values.getKeys(false);

        for (String key : keys) {
            ConfigurationSection value = values.getConfigurationSection(key);
            ItemStack valueItemStack = getValueItemStack(value);

            if (valueItemStack == null) {
                continue;
            }

            Set<String> keys1 = value.getKeys(false);

            if (keys1.contains("index")) {
                List<Integer> indexs = getIndexList(value);

                for (Integer integer : indexs) {
                    inventory.setItem(integer, valueItemStack);
                }

                if (keys1.contains("mode")) {
                    String mode = value.getString("mode");

                    if (mode.equalsIgnoreCase("reward")) {
                        String rewardId = value.getString("rewardId");
                        if (rewardId == null || rewardId.isEmpty()){
                            ctOnlineReward.getLogger().warning("配置错误，没有找到对应的rewardId  错误位置:"+key);
                        }
                        handleRewardMode(rewardId, indexs);
                    } else if (mode.equalsIgnoreCase("command")) {
                        handleCommandMode(value, indexs);
                    } else if (mode.equalsIgnoreCase("gui")) {
                        handleGuiMode(value, indexs);
                    }

                    handleModeMap(mode, indexs);
                }
            }
        }
    }

    private List<Integer> getIndexList(ConfigurationSection value) {
        List<Integer> indexs = new ArrayList<>();
        Object index = value.get("index");

        if (index instanceof Integer) {
            indexs.add((Integer) index);
        } else {
            String x = value.getString("index.x");
            String y = value.getString("index.y");
            indexs = Position.get(x, y);
        }

        return indexs;
    }

    private void handleRewardMode(String rewardId, List<Integer> indexs) {
        RewardEntity rewardEntity = rewardSet.stream().filter(it -> it.getRewardID().equals(rewardId)).findFirst().get();
        Map<Integer, RewardEntity> statusMap = mainInventoryHolder.statusMap;
        for (Integer integer : indexs) {
            statusMap.put(integer, rewardEntity);
        }
    }

    private void handleCommandMode(ConfigurationSection value, List<Integer> indexs) {
        ConfigurationSection configurationSection = getItemStackCommand(value);
        for (Integer integer : indexs) {
            mainInventoryHolder.commandMap.put(integer, configurationSection);
        }
    }

    private void handleGuiMode(ConfigurationSection value, List<Integer> indexs) {
        if (value.contains("gui")) {
            String gui = value.getString("gui");
            for (Integer integer : indexs) {
                mainInventoryHolder.guiMap.put(integer, gui);
            }
        }
    }

    private void handleModeMap(String mode, List<Integer> indexs) {
        Map<Integer, String> modeMap = mainInventoryHolder.modeMap;

        for (Integer integer : indexs) {
            modeMap.put(integer, mode);
        }
    }

    private ConfigurationSection getItemStackCommand(ConfigurationSection value) {
        if (!value.contains("command")) {
            return null;
        }
        return value.getConfigurationSection("command");
    }

    private ItemStack getValueItemStack(ConfigurationSection value) {
        ItemStack itemStack = getItemStackType(null, value);
        ItemMeta itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMetaHandler(value, itemMeta);
        itemStack.setItemMeta(itemMeta);

        if (value.contains("mode")) {
            String mode = value.getString("mode");
            if (mode.equalsIgnoreCase("reward")) {
                itemStack = extendHandler(itemStack, value, value.getString("rewardId"));
            }
        }

        return itemStack;
    }

    private ItemStack extendHandler(ItemStack itemStack, ConfigurationSection value, String rewardId) {
        RewardStatus rewardStatus = getRewardStatus(player, rewardId);
        RewardEntity rewardEntity = new RewardEntity(rewardId, rewardStatus);

        if (!value.contains("extend")) {
            rewardSet.add(rewardEntity);
            return itemStack;
        }

        ConfigurationSection extend = value.getConfigurationSection("extend");
        ConfigurationSection targetSection = null;

        switch (rewardStatus) {
            case before:
                targetSection = extend.getConfigurationSection("before");
                break;
            case after:
                targetSection = extend.getConfigurationSection("after");
                break;
            case activation:
                targetSection = extend.getConfigurationSection("activation");
                break;
        }

        if (targetSection != null) {
            itemStack = getItemStackType(itemStack,targetSection);
            ItemMeta itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
            itemMetaHandler(targetSection, itemMeta);
            itemStack.setItemMeta(itemMeta);
        }

        rewardSet.add(rewardEntity);
        return itemStack;
    }

    private ItemStack getItemStackType(ItemStack itemStack, ConfigurationSection config) {
        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        if (!config.contains("type")) {
            config = plugin.getConfig().getConfigurationSection("Setting.defaultItemType");
        }

        String type = config.getString("type.name", "chest");
        if (type.equalsIgnoreCase("skull")) {
            String skull = config.getString("type.skull");
            itemStack = ItemUtils.crearSkull(skull);
        } else {
            itemStack = getItemStackByNMS(type);
        }

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            itemStack = new ItemStack(Material.CHEST);
        }

        boolean enchantment = config.getBoolean("type.enchantment");
        if (enchantment) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        } else {
            itemStack.removeEnchantment(Enchantment.DURABILITY);
        }

        return itemStack;
    }

    private ItemStack getItemStackByNMS(String name) {
        if (isInteger(name)) {
            return new ItemStack(Material.getMaterial(Integer.parseInt(name)));
        }

        if (name.startsWith("minecraft:")) {
            String materialName = name.substring(10).toUpperCase();
            Material material = Material.getMaterial(materialName);
            if (material != null) {
                return new ItemStack(material);
            }
            return null;
        }

        String versionString = Util.getVersionString();
        try {
            String className = "net.minecraft.server." + versionString + ".";
            Class<?> itemStackClass = Class.forName(className + "ItemStack");
            Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit." + versionString + ".inventory.CraftItemStack");

            Class<?> itemClass;
            try {
                itemClass = Class.forName(className + "Item");
            } catch (ClassNotFoundException e) {
                itemClass = Class.forName(className + "IRegistry").getField("ITEM").get(null).getClass();
            }

            Object invoke;
            try {
                Method b = itemClass.getMethod("b", String.class);
                invoke = b.invoke(itemClass, name);
            } catch (NoSuchMethodException e) {
                Object itemRegistry = Class.forName(className + "IRegistry").getField("ITEM").get(null);
                Object key = Class.forName(className + "MinecraftKey").getConstructor(String.class).newInstance(name);
                Method getMethod = itemRegistry.getClass().getMethod("get", Class.forName(className + "MinecraftKey"));
                invoke = getMethod.invoke(itemRegistry, key);
            }

            Constructor<?> itemStackConstructor;
            try {
                itemStackConstructor = itemStackClass.getDeclaredConstructor(itemClass);
            } catch (NoSuchMethodException e) {
                itemStackConstructor = itemStackClass.getDeclaredConstructor(Class.forName(className + "IMaterial"));
            }

            Object nmsItemStack = itemStackConstructor.newInstance(invoke);
            Method asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", itemStackClass);
            return (ItemStack) asBukkitCopy.invoke(craftItemStack, nmsItemStack);
        } catch (Exception e) {
            throw new RuntimeException("GUI物品材质名称配置错误! 错误的材质名称: " + name, e);
        }
    }

    private void itemMetaHandler(ConfigurationSection config, ItemMeta itemMeta) {
        if (config.contains("name")) {
            String name = config.getString("name").replace("&", "§");
            String s = PlaceholderAPI.setPlaceholders(player, name);
            itemMeta.setDisplayName(s);
        }
        if (config.contains("lore")) {
            List<String> lore = config.getStringList("lore");
            lore.replaceAll(line -> line.replace("&", "§"));
            List<String> processedLore = PlaceholderAPI.setPlaceholders(player, lore);
            itemMeta.setLore(processedLore);
        }
        if (config.contains("customModelData")) {

            int customModelData = config.getInt("customModelData");
            try {
                Method setCustomModelData = itemMeta.getClass().getMethod("setCustomModelData", Integer.class);
                setCustomModelData.setAccessible(true);
                setCustomModelData.invoke(itemMeta, customModelData);
            } catch (Exception e) {
                ctOnlineReward.getLogger().warning("§c§l Failed to set CustomModelData for the item! (Unsupported in this version)");
            }
        }

        if (itemMeta instanceof SkullMeta && config.contains("skull")) {
            String skull = config.getString("skull");
            boolean setOwnerSuccess = ((SkullMeta) itemMeta).setOwner(skull);
            if (!setOwnerSuccess) {
                ctOnlineReward.getLogger().warning("§c§l 头颅读取失败！");
            }
        }
    }

    private RewardStatus getRewardStatus(Player player, String rewardId) {
        ConfigurationSection configurationSection = YamlData.rewardYaml.getConfigurationSection(rewardId);
        if (configurationSection == null) {
            ctOnlineReward.getLogger().warning("§c§l■ 未找到奖励配置 §f§n" + rewardId + "§c§l 请检查reward.yml配置文件中是否有指定配置!");
            return RewardStatus.before;
        }
        if (!configurationSection.contains("time")) {
            return RewardStatus.before;
        }
        boolean timeIsOk = RewardOnlineTimeHandler.getInstance().onlineTimeIsOk(player, configurationSection.getString("time"));
        if (!timeIsOk) {
            return RewardStatus.before;
        }
        List<String> playerRewardArray = CtOnlineReward.dataService.getPlayerRewardArray(player);
        if (playerRewardArray.isEmpty() || !playerRewardArray.contains(rewardId)) {
            return RewardStatus.activation;
        }
        return RewardStatus.after;
    }


}
