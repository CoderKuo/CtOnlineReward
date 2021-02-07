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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RewardSetInventory {
    private CtOnlineReward ctOnlineReward;

    public RewardSetInventory(){
        ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    }



    public void openInventory(Player player,String reward){

        RewardSetInventoryHolder holder = new RewardSetInventoryHolder(reward);
        Inventory inventory = Bukkit.createInventory(holder, 45, "§7§l奖励设置[" + reward + "]");
        Map<Integer, ItemStack> frameItemStackMap = getFrameItemStackMap();
        Set<Integer> integers = frameItemStackMap.keySet();
        for (Integer integer : integers) {
            inventory.setItem(integer,frameItemStackMap.get(integer));
        }

        File file = new File(ctOnlineReward.getDataFolder() + "/rewardData/" + reward);
        if(file.exists()){
            RewardService instance = RewardService.getInstance();
            List<ItemStack> itemStackFromFile = instance.getItemStackFromFile(file);
            if(itemStackFromFile != null){
                for (ItemStack itemStack : itemStackFromFile) {
                    inventory.setItem(inventory.firstEmpty(),itemStack);
                }
            }
        }

        player.openInventory(inventory);

    }

    private Map<Integer, ItemStack> getFrameItemStackMap(){
        Map<Integer,ItemStack> map = new HashMap<>();
        ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE);
        ItemMeta itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMeta.setDisplayName("-");
        itemStack.setItemMeta(itemMeta);
        for (int i = 36; i < 45; i++) {
            if(i == 40){
                continue;
            }
            map.put(i,itemStack);
        }
        ItemStack saveItem = new ItemStack(Material.REDSTONE);
        ItemMeta saveItemMeta = saveItem.hasItemMeta() ? saveItem.getItemMeta() : Bukkit.getItemFactory().getItemMeta(saveItem.getType());
        saveItemMeta.setDisplayName("§c§l▲保存配置");
        saveItem.setItemMeta(saveItemMeta);
        map.put(40,saveItem);
        return map;
    }
}
