package cn.ctcraft.ctonlinereward.inventory;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.service.RewardService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class RewardSetInventory {
    private final CtOnlineReward ctOnlineReward;

    public RewardSetInventory() {
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    }

    public void openInventory(Player player, String reward) {
        Inventory inventory = createInventory(reward);
        player.openInventory(inventory);
    }

    private Inventory createInventory(String reward) {
        RewardSetInventoryHolder holder = new RewardSetInventoryHolder(reward);
        Inventory inventory = Bukkit.createInventory(holder, 45, "§7§l奖励设置[" + reward + "]");
        Map<Integer, ItemStack> frameItemStackMap = getFrameItemStackMap();
        frameItemStackMap.forEach(inventory::setItem);

        File file = new File(ctOnlineReward.getDataFolder() + "/rewardData/" + reward);
        if (file.exists()) {
            RewardService instance = RewardService.getInstance();
            List<ItemStack> itemStackFromFile = instance.getItemStackFromFile(file);
            if (itemStackFromFile != null) {
                itemStackFromFile.stream()
                        .filter(Objects::nonNull)
                        .forEach(inventory::addItem);
            }
        }

        return inventory;
    }

    private Map<Integer, ItemStack> getFrameItemStackMap() {
        Map<Integer, ItemStack> map = new HashMap<>();
        ItemStack frameItemStack = createFrameItemStack();
        for (int i = 36; i < 45; i++) {
            if (i != 40) {
                map.put(i, frameItemStack);
            }
        }

        ItemStack saveItemStack = createSaveItemStack();
        map.put(40, saveItemStack);

        return map;
    }

    private ItemStack createFrameItemStack() {
        ItemStack frameItemStack = new ItemStack(Material.STONE);
        ItemMeta frameItemMeta = frameItemStack.getItemMeta();
        frameItemMeta.setDisplayName("-");
        frameItemStack.setItemMeta(frameItemMeta);
        return frameItemStack;
    }

    private ItemStack createSaveItemStack() {
        ItemStack saveItemStack = new ItemStack(Material.REDSTONE);
        ItemMeta saveItemMeta = saveItemStack.getItemMeta();
        saveItemMeta.setDisplayName("§c§l▲保存配置");
        saveItemStack.setItemMeta(saveItemMeta);
        return saveItemStack;
    }
}
