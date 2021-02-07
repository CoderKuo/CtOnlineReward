package cn.ctcraft.ctonlinereward.inventory;

import cn.ctcraft.ctonlinereward.RewardEntity;
import cn.ctcraft.ctonlinereward.service.RewardStatus;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class MainInventoryHolder implements InventoryHolder {
    public Map<Integer, RewardEntity> statusMap = new HashMap<>();

    @Override
    public Inventory getInventory() {
        return null;
    }
}
