package cn.ctcraft.ctonlinereward.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class RewardSetInventoryHolder implements InventoryHolder {
    private String reward;

    public RewardSetInventoryHolder(String reward){
        this.reward = reward;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
