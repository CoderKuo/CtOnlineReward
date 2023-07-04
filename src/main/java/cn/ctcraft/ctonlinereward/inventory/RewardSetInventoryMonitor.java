package cn.ctcraft.ctonlinereward.inventory;

import cn.ctcraft.ctonlinereward.pojo.RewardData;
import cn.ctcraft.ctonlinereward.service.RewardService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
        Inventory inventory = e.getInventory();
        if(inventory == null){
            return;
        }

        InventoryHolder holder = inventory.getHolder();
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
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR){
                    itemStacks.add(inventory.getItem(i));
                }
            }
            String reward = ((RewardSetInventoryHolder) holder).getReward();
            RewardData rewardData = new RewardData(itemStacks);

            boolean b = rewardService.saveRewardData(rewardData, reward);
            if(b){
                ((Player)e.getWhoClicked()).sendMessage("§a§l● "+reward+"奖励数据保存成功!");
            }
            e.getWhoClicked().closeInventory();
        }

    }
}
