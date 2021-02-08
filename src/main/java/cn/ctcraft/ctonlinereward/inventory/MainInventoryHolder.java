package cn.ctcraft.ctonlinereward.inventory;

import cn.ctcraft.ctonlinereward.RewardEntity;
import cn.ctcraft.ctonlinereward.service.RewardStatus;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class MainInventoryHolder implements InventoryHolder {
    public Map<Integer,String> modeMap = new HashMap<>();
    public Map<Integer, RewardEntity> statusMap = new HashMap<>();
    public Map<Integer, YamlConfiguration> commandMap = new HashMap<>();
    public Map<Integer,String> guiMap = new HashMap<>();

    @Override
    public Inventory getInventory() {
        return null;
    }
}
