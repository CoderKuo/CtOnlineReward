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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class InventoryMonitor implements Listener {
    private RewardService rewardService = RewardService.getInstance();
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private InventoryHolder holder = null;

    @EventHandler
    public void InventoryClick(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory == null || e.getRawSlot() < 0 || e.getClickedInventory() == null) {
            return;
        }
        e.getCursor();
        holder = inventory.getHolder();
        if (!(holder instanceof MainInventoryHolder)) {
            return;
        }

        if (e.getClick().isShiftClick() || e.getClickedInventory().getHolder() instanceof MainInventoryHolder) {
            e.setCancelled(true);
        }

        int rawSlot = e.getRawSlot();
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

        List<String> playerCommands = keys.contains("PlayerCommands") ? command.getStringList("PlayerCommands") : null;
        List<String> opCommands = keys.contains("OpCommands") ? command.getStringList("OpCommands") : null;
        List<String> consoleCommands = keys.contains("ConsoleCommands") ? command.getStringList("ConsoleCommands") : null;

        if (playerCommands != null) {
            List<String> list = PlaceholderAPI.setPlaceholders(player, playerCommands);
            for (String s : list) {
                player.performCommand(s);
            }
        }

        boolean isOp = player.isOp();
        try {
            player.setOp(true);
            if (opCommands != null) {
                List<String> list1 = PlaceholderAPI.setPlaceholders(player, opCommands);
                for (String c : list1) {
                    player.performCommand(c);
                }
            }
        } finally {
            player.setOp(isOp);
        }

        if (consoleCommands != null) {
            List<String> list2 = PlaceholderAPI.setPlaceholders(player, consoleCommands);
            for (String s : list2) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
            }
        }
    }

    private void rewardExecute(RewardEntity rewardEntity, Player player) {
        if (rewardEntity.getStatus() != RewardStatus.activation) {
            return;
        }

        List<String> playerRewardArray = CtOnlineReward.dataService.getPlayerRewardArray(player);
        if (playerRewardArray.contains(rewardEntity.getRewardID())) {
            if (holder instanceof MainInventoryHolder) {
                Inventory build = InventoryFactory.build(((MainInventoryHolder) holder).inventoryID, player);
                player.openInventory(build);
            }
            return;
        }

        try {
            if (permissionHandler(rewardEntity.getRewardID(), player)) {
                List<ItemStack> itemStackFromRewardId = rewardService.getItemStackFromRewardId(rewardEntity.getRewardID());
                DataService playerDataService = CtOnlineReward.dataService;

                if (!isPlayerInventorySizeEnough(itemStackFromRewardId, player)) {
                    player.sendMessage(CtOnlineReward.languageHandler.getLang("reward.volume").replace("{rewardSize}", String.valueOf(itemStackFromRewardId.size())));
                    player.sendMessage(CtOnlineReward.languageHandler.getLang("reward.volume2"));
                    return;
                }

                if (playerDataService.addRewardToPlayData(rewardEntity.getRewardID(), player)) {
                    Sound sound = getSound(rewardEntity.getRewardID());
                    if (sound != null) {
                        player.playSound(player.getLocation(), sound, 1F, 1F);
                    }

                    if (itemStackFromRewardId != null) {
                        givePlayerItem(itemStackFromRewardId, player);
                    }

                    executeCommand(rewardEntity.getRewardID(), player);
                    giveMoney(player, rewardEntity.getRewardID());
                    action(rewardEntity.getRewardID(), player);
                }
            } else {
                String lang = CtOnlineReward.languageHandler.getLang("reward.volume3");
                player.sendMessage(lang);
            }
        } catch (Exception ex) {
            ctOnlineReward.getLogger().warning("§c§l■ 奖励配置异常!");
            ex.printStackTrace();
        }
    }


    private boolean permissionHandler(String rewardId, Player player) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        if (!rewardYaml.contains(rewardId)) {
            return true;
        }
        ConfigurationSection configurationSection = rewardYaml.getConfigurationSection(rewardId);
        String permission = configurationSection.getString("permission");
        return permission == null || player.hasPermission(permission);
    }


    private void action(String rewardID, Player player) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        if (!rewardYaml.contains(rewardID)) {
            return;
        }
        ConfigurationSection configurationSection = rewardYaml.getConfigurationSection(rewardID);
        if (!configurationSection.contains("receiveAction")) {
            return;
        }
        List<String> receiveAction = configurationSection.getStringList("receiveAction");
        receiveAction.forEach(action -> actionHandler(action, player, configurationSection));
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
        if (!rewardYaml.contains(rewardID + ".sound")) {
            return null;
        }
        String sound = rewardYaml.getString(rewardID + ".sound");
        try {
            return Sound.valueOf(sound);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void giveMoney(Player player, String rewardID) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        if (!rewardYaml.contains(rewardID + ".economy")) {
            return;
        }
        ConfigurationSection economy = rewardYaml.getConfigurationSection(rewardID + ".economy");
        if (economy.contains("money")) {
            double money = economy.getDouble("money");
            CtOnlineReward.economy.depositPlayer(player, money);
        }
        if (economy.contains("points")) {
            int points = economy.getInt("points");
            try {
                PlayerPointsAPI playerPointsAPI = new PlayerPointsAPI(ctOnlineReward.getPlayerPoints());
                playerPointsAPI.give(player.getUniqueId(), points);
            } catch (NoClassDefFoundError e) {
                ctOnlineReward.getLogger().warning("§c§l■ 未找到点券插件,请勿在配置文件(reward.yml)中配置点券项，如果需要使用点券请安装PlayerPoints");
            }
        }
    }

    private void executeCommand(String rewardID, Player player) {
        YamlConfiguration rewardYaml = YamlData.rewardYaml;
        if (!rewardYaml.contains(rewardID + ".command")) {
            return;
        }
        ConfigurationSection command = rewardYaml.getConfigurationSection(rewardID + ".command");
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
        if (!isPlayerInventorySizeEnough(list, player)) {
            return false;
        }

        PlayerInventory inventory = player.getInventory();
        list.forEach(itemStack -> {
            if (itemStack != null) {
                inventory.addItem(itemStack);
            }
        });

        return true;
    }


    private boolean isPlayerInventorySizeEnough(List<ItemStack> itemStacks, Player player) {
        PlayerInventory inventory = player.getInventory();
        int emptySize = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                emptySize++;
            }
        }

        itemStacks.removeIf(Objects::isNull);

        return itemStacks.size() <= emptySize;
    }

}
