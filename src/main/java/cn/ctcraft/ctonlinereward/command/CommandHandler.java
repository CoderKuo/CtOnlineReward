package cn.ctcraft.ctonlinereward.command;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.service.RemindTimer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandHandler implements CommandExecutor {
    private static CommandHandler instance = new CommandHandler();
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private CommandExecute commandExecute = CommandExecute.getInstance();

    public static CommandHandler getInstance() {
        return instance;
    }

    private CommandHandler() {

    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("cor")) {
            return true;
        }
        if (args.length == 0) {
            if (sender instanceof Player) {
                if (!sender.hasPermission("CtOnlineReward.cor")) {
                    sender.sendMessage("§c§l权限不足!");
                    return true;
                }
                commandExecute.openInventory(sender, new String[]{"1"});

            } else {
                sender.sendMessage(ctOnlineReward.getDescription().getName());
                sender.sendMessage(ctOnlineReward.getDescription().getVersion());

            }
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reward")) {
                commandExecute.openRewardSetInventory(sender, args);
                return true;
            }
            if (args[0].equalsIgnoreCase("open")) {
                commandExecute.openInventory(sender, args);
                return true;
            }
            if (args[0].equalsIgnoreCase("reload") && args.length == 1 && sender.hasPermission("CtOnlineReward.reload")) {
                reload();
                sender.sendMessage("§c§l重载成功!");
                return true;
            }
            if (args[0].equalsIgnoreCase("remind") && args.length == 2) {
                if (args[1].equalsIgnoreCase("on")) {
                    List<Player> players = RemindTimer.players;
                    if (players.contains((Player)sender)) {
                        players.remove(sender);
                    }
                    sender.sendMessage("§a§l成功打开提醒！");
                    return true;
                }
                if (args[1].equalsIgnoreCase("off")) {
                    List<Player> players = RemindTimer.players;
                    if (!players.contains((Player)sender)) {
                        players.add((Player) sender);
                    }
                    sender.sendMessage("§c§l成功关闭提醒!");
                    return true;
                }
            }
        }


        return true;
    }

    private void reload() {
        ctOnlineReward.load();
    }
}
