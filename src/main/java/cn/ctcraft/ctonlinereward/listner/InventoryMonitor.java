package cn.ctcraft.ctonlinereward.listner;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.RewardEntity;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.inventory.InventoryFactory;
import cn.ctcraft.ctonlinereward.inventory.MainInventoryHolder;
import cn.ctcraft.ctonlinereward.service.PlayerDataService;
import cn.ctcraft.ctonlinereward.service.RewardService;
import cn.ctcraft.ctonlinereward.service.RewardStatus;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.bukkit.Sound.BLOCK_CHEST_OPEN;
import static org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;

public class InventoryMonitor implements Listener {
    private RewardService rewardService = RewardService.getInstance();
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    @EventHandler
    public void InventoryClick(InventoryClickEvent e){
        Inventory clickedInventory = e.getClickedInventory();
        if(clickedInventory == null){
            return;
        }
        InventoryHolder holder = clickedInventory.getHolder();
        if(!(holder instanceof MainInventoryHolder)){
            return;
        }
        e.setCancelled(true);
        if (e.getRawSlot() < 0 || e.getRawSlot() > e.getInventory().getSize() || e.getInventory() == null) {
            return;
        }
        Map<Integer, RewardEntity> statusMap = ((MainInventoryHolder) holder).statusMap;
        if(statusMap.containsKey(e.getRawSlot())){
            RewardEntity rewardEntity = statusMap.get(e.getRawSlot());
            if(rewardEntity.getStatus() == RewardStatus.activation){
                try {
                    Player player = (Player) e.getWhoClicked();
                    player.playSound(player.getLocation(),ENTITY_PLAYER_LEVELUP ,1F,1F);

                    List<ItemStack> itemStackFromRewardId = rewardService.getItemStackFromRewardId(rewardEntity.getRewardID());
                    boolean b = givePlayerItem(itemStackFromRewardId, player);
                    if(b){
                        executeCommand(rewardEntity.getRewardID(),player);
                        PlayerDataService playerDataService = PlayerDataService.getInstance();
                        boolean b1 = playerDataService.addRewardToPlayData(rewardEntity.getRewardID(), player);
                        if(b1){
                            player.sendMessage("§a§l● 奖励领取成功!");
                            Inventory build = InventoryFactory.build("menu.yml", player);
                            player.openInventory(build);
                        }
                    }


                }catch (Exception ex){
                    ctOnlineReward.getLogger().warning("§c§l■ 奖励配置异常!");
                    ex.printStackTrace();
                }
            }
        }

    }

    private void executeCommand(String rewardID,Player player){
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        Set<String> rewardYamlKeys = rewardYaml.getKeys(false);
        if(!rewardYamlKeys.contains(rewardID)){
            return;
        }
        ConfigurationSection rewardIdYaml = rewardYaml.getConfigurationSection(rewardID);
        Set<String> rewardIdYamlKeys = rewardIdYaml.getKeys(false);
        if(!rewardIdYamlKeys.contains("command")){
            return;
        }
        ConfigurationSection command = rewardIdYaml.getConfigurationSection("command");
        List<String> playerCommands = command.getStringList("PlayerCommands");
        List<String> list = PlaceholderAPI.setPlaceholders(player, playerCommands);
        for (String s : list) {
            player.performCommand(s);
        }
        List<String> opCommands = command.getStringList("OpCommands");
        List<String> list1 = PlaceholderAPI.setPlaceholders(player, opCommands);
        boolean isOp = player.isOp();
        try {
            player.setOp(true);
            for (String c : list1) {
                player.performCommand(c);
            }
        } finally {
            player.setOp(isOp);
        }

        List<String> consoleCommands = command.getStringList("ConsoleCommands");
        List<String> list2 = PlaceholderAPI.setPlaceholders(player, consoleCommands);
        for (String s : list2) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),s);
        }

    }

    private boolean givePlayerItem(List<ItemStack> list, Player player){
        PlayerInventory inventory = player.getInventory();
        int size = 0;
        for (int i = 0; i < 36; i++) {
            if(inventory.getItem(i) == null){
                size++;
            }
        }

        for (int i = 0; i < list.size(); i++) {
            ItemStack itemStack = list.get(i);
            if(itemStack == null){
                list.remove(i);
            }
        }


        if(list.size() > size){
            player.sendMessage("§c§l■ 此奖励需要"+list.size()+"格背包空间");
            player.sendMessage("§c§l■ 背包空间不足,请先清空背包!");
            return false;
        }
        for (ItemStack itemStack : list) {
            if(itemStack != null){
                inventory.addItem(itemStack);
            }
        }

        return true;
    }
}
