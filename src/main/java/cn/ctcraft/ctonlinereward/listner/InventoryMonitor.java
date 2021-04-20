package cn.ctcraft.ctonlinereward.listner;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.RewardEntity;
import cn.ctcraft.ctonlinereward.database.DataService;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.inventory.ActionType;
import cn.ctcraft.ctonlinereward.inventory.InventoryFactory;
import cn.ctcraft.ctonlinereward.inventory.MainInventoryHolder;
import cn.ctcraft.ctonlinereward.service.RewardService;
import cn.ctcraft.ctonlinereward.service.RewardStatus;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.EconomyResponse;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.xml.ws.Holder;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class InventoryMonitor implements Listener {
    private RewardService rewardService = RewardService.getInstance();
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private InventoryHolder holder = null;

    @EventHandler
    public void InventoryClick(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory == null) {
            return;
        }
        holder = inventory.getHolder();
        if (!(holder instanceof MainInventoryHolder)) {
            return;
        }
        int rawSlot = e.getRawSlot();
        if (e.getRawSlot() < 0 || e.getInventory() == null) {
            return;
        }
        if (e.getClick().isShiftClick()) {
            e.setCancelled(true);
        }
        if (e.getClickedInventory().getHolder() instanceof MainInventoryHolder) {
            e.setCancelled(true);
        }

        MainInventoryHolder mainInventoryHolder = (MainInventoryHolder) holder;
        Map<Integer, RewardEntity> statusMap = mainInventoryHolder.statusMap;
        Player player = (Player) e.getWhoClicked();
        if (statusMap.containsKey(rawSlot)) {
            rewardExecute(statusMap.get(rawSlot), player);
        }
        Map<Integer, ConfigurationSection> commandMap = mainInventoryHolder.commandMap;
        if (commandMap.containsKey(rawSlot)) {
            commandExecute(commandMap.get(rawSlot), player);
        }
        Map<Integer, String> guiMap = mainInventoryHolder.guiMap;
        if (guiMap.containsKey(rawSlot)) {
            guiExecute(guiMap.get(rawSlot), player);
        }

    }

    private void guiExecute(String gui, Player player) {
        Inventory build = InventoryFactory.build(gui, player);
        player.openInventory(build);
    }

    private void commandExecute(ConfigurationSection command, Player player) {
        Set<String> keys = command.getKeys(false);
        if (keys.contains("PlayerCommands")) {
            List<String> playerCommands = command.getStringList("PlayerCommands");
            List<String> list = PlaceholderAPI.setPlaceholders(player, playerCommands);
            for (String s : list) {
                player.performCommand(s);
            }
        }
        if (keys.contains("OpCommands")) {
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
        }
        if (keys.contains("ConsoleCommands")) {
            List<String> consoleCommands = command.getStringList("ConsoleCommands");
            List<String> list2 = PlaceholderAPI.setPlaceholders(player, consoleCommands);
            for (String s : list2) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
            }
        }

    }

    private void rewardExecute(RewardEntity rewardEntity, Player player) {
        if (rewardEntity.getStatus() == RewardStatus.activation) {
            List<String> playerRewardArray = CtOnlineReward.dataService.getPlayerRewardArray(player);
            if (playerRewardArray.contains(rewardEntity.getRewardID())) {
                if (holder != null) {
                    if (holder instanceof MainInventoryHolder) {
                        Inventory build = InventoryFactory.build(((MainInventoryHolder) holder).inventoryID, player);
                        player.openInventory(build);
                    }
                }
                return;
            }
            try {
                boolean b = permissionHandler(rewardEntity.getRewardID(), player);
                if (b){
                    DataService playerDataService = CtOnlineReward.dataService;
                    boolean b1 = playerDataService.addRewardToPlayData(rewardEntity.getRewardID(), player);
                    if (b1) {
                        Sound sound = getSound(rewardEntity.getRewardID());
                        if (sound != null) {
                            player.playSound(player.getLocation(), sound, 1F, 1F);
                        }

                        List<ItemStack> itemStackFromRewardId = rewardService.getItemStackFromRewardId(rewardEntity.getRewardID());
                        if (itemStackFromRewardId != null) {
                            givePlayerItem(itemStackFromRewardId, player);
                        }
                        executeCommand(rewardEntity.getRewardID(), player);
                        giveMoney(player, rewardEntity.getRewardID());
                        action(rewardEntity.getRewardID(), player);
                    }
                }else{
                    String lang = CtOnlineReward.languageHandler.getLang("reward.volume3");
                    player.sendMessage(lang);

                }


            } catch (Exception ex) {
                ctOnlineReward.getLogger().warning("§c§l■ 奖励配置异常!");
                ex.printStackTrace();
            }
        }
    }

    private boolean permissionHandler(String rewardId,Player player){
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        Set<String> rewardYamlKeys = rewardYaml.getKeys(false);
        if (!rewardYamlKeys.contains(rewardId)) {
            return true;
        }
        ConfigurationSection configurationSection = rewardYaml.getConfigurationSection(rewardId);
        Set<String> keys = configurationSection.getKeys(false);
        if (!keys.contains("permission")) {
            return true;
        }
        String permission = configurationSection.getString("permission");
        return player.hasPermission(permission);

    }

    private void action(String rewardID, Player player) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        Set<String> rewardYamlKeys = rewardYaml.getKeys(false);
        if (!rewardYamlKeys.contains(rewardID)) {
            return;
        }
        ConfigurationSection configurationSection = rewardYaml.getConfigurationSection(rewardID);
        Set<String> keys = configurationSection.getKeys(false);
        if (!keys.contains("receiveAction")) {
            return;
        }
        List<String> receiveAction = configurationSection.getStringList("receiveAction");
        for (String action : receiveAction) {
            actionHandler(action, player, configurationSection);
        }
    }

    private void actionHandler(String actionContent, Player player, ConfigurationSection configurationSection) {
        ActionType actionType = ActionType.getActionType(actionContent);
        if (actionType == null) {
            return;
        }
        switch (actionType) {
            case sound:
                String soundText = actionContent.replace("[sound]", "").replace(" ", "");
                Sound sound = Sound.valueOf(soundText);
                player.playSound(player.getLocation(), sound, 1, 1);
                break;
            case Message:
                String messageText = actionContent.replace("[Message]", "").replace(" ", "").replace("&", "§");
                int moneyNum = configurationSection.getInt("economy.money");
                messageText = messageText.replace("{money}", String.valueOf(moneyNum));
                int pointsNum = configurationSection.getInt("economy.points");
                messageText = messageText.replace("{points}", String.valueOf(pointsNum));
                player.sendMessage(messageText);
                break;
            case closeGUI:
                player.closeInventory();
                break;
            case openGUI:
                String guiText = actionContent.replace("[openGUI]", "").replace(" ", "");
                Inventory build = InventoryFactory.build(guiText, player);
                player.openInventory(build);
                break;
        }
    }


    private Sound getSound(String rewardID) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        Set<String> rewardYamlKeys = rewardYaml.getKeys(false);
        if (!rewardYamlKeys.contains(rewardID)) {
            return null;
        }
        ConfigurationSection rewardIdYaml = rewardYaml.getConfigurationSection(rewardID);
        Set<String> rewardIdYamlKeys = rewardIdYaml.getKeys(false);
        if (!rewardIdYamlKeys.contains("sound")) {
            return null;
        }
        String sound = rewardIdYaml.getString("sound");
        try {
            return Sound.valueOf(sound);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void giveMoney(Player player, String rewardID) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        Set<String> rewardYamlKeys = rewardYaml.getKeys(false);
        if (!rewardYamlKeys.contains(rewardID)) {
            return;
        }
        ConfigurationSection rewardIdYaml = rewardYaml.getConfigurationSection(rewardID);
        Set<String> rewardIdYamlKeys = rewardIdYaml.getKeys(false);
        if (!rewardIdYamlKeys.contains("economy")) {
            return;
        }
        ConfigurationSection economy = rewardIdYaml.getConfigurationSection("economy");
        Set<String> keys = economy.getKeys(false);
        if (keys.contains("money")) {
            double money = economy.getDouble("money");
            CtOnlineReward.economy.depositPlayer(player, money);
        }
        if (keys.contains("points")) {
            int points = economy.getInt("points");
            PlayerPointsAPI playerPointsAPI = new PlayerPointsAPI(ctOnlineReward.getPlayerPoints());
            playerPointsAPI.give(player.getUniqueId(), points);
        }
    }

    private void executeCommand(String rewardID, Player player) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        Set<String> rewardYamlKeys = rewardYaml.getKeys(false);
        if (!rewardYamlKeys.contains(rewardID)) {
            return;
        }
        ConfigurationSection rewardIdYaml = rewardYaml.getConfigurationSection(rewardID);
        Set<String> rewardIdYamlKeys = rewardIdYaml.getKeys(false);
        if (!rewardIdYamlKeys.contains("command")) {
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
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
        }

    }

    private boolean givePlayerItem(List<ItemStack> list, Player player) {
        PlayerInventory inventory = player.getInventory();
        int size = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) {
                size++;
            }
        }

        for (int i = 0; i < list.size(); i++) {
            ItemStack itemStack = list.get(i);
            if (itemStack == null) {
                list.remove(i);
            }
        }


        if (list.size() > size) {
            player.sendMessage(CtOnlineReward.languageHandler.getLang("reward.volume").replace("{rewardSize}", String.valueOf(list.size())));
            player.sendMessage(CtOnlineReward.languageHandler.getLang("reward.volume2"));
            return false;
        }
        for (ItemStack itemStack : list) {
            if (itemStack != null) {
                inventory.addItem(itemStack);
            }
        }

        return true;
    }
}
