package cn.ctcraft.ctonlinereward.inventory;

import cn.ctcraft.ctonlinereward.service.RewardService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RewardSetInventoryMonitor implements Listener {
    private static RewardSetInventoryMonitor instance = new RewardSetInventoryMonitor();
    private RewardService rewardService;
    private RewardSetInventoryMonitor(){
        rewardService = RewardService.getInstance();
    }

    public static RewardSetInventoryMonitor getInstance(){
        return instance;
    }


    @EventHandler
    public void Monitor(InventoryClickEvent e){
        Inventory clickedInventory = e.getClickedInventory();
        if(clickedInventory == null){
            return;
        }

        InventoryHolder holder = clickedInventory.getHolder();
        if(!(holder instanceof RewardSetInventoryHolder)){
            return;
        }
        int rawSlot = e.getRawSlot();
        if(rawSlot <= 44 && rawSlot >= 36){
            e.setCancelled(true);
        }
        if(rawSlot == 40){
            List<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < 36; i++) {
                itemStacks.add(clickedInventory.getItem(i));
            }
            String reward = ((RewardSetInventoryHolder) holder).getReward();
            boolean b = rewardService.saveRewardDate(itemStacks, reward);
            if(b){
                e.getWhoClicked().sendMessage("§a§l● "+reward+"奖励数据保存成功!");
            }
            e.getWhoClicked().closeInventory();
        }

    }
}
