package cn.ctcraft.ctonlinereward.command;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandHandler implements CommandExecutor {
    private static final CommandHandler instance = new CommandHandler();
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
                Player player = (Player) sender;
                if (!player.hasPermission("CtOnlineReward.cor")) {
                    sender.sendMessage("§c§l权限不足!");
                    return true;
                }
                commandExecute.openInventory(player, new String[]{"1"});
            } else {
                sender.sendMessage(ctOnlineReward.getDescription().getName());
                sender.sendMessage(ctOnlineReward.getDescription().getVersion());
            }
        } else {
            String arg = args[0].toLowerCase();
            switch (arg) {
                case "reward":
                    commandExecute.openRewardSetInventory(sender, args);
                    break;
                case "open":
                    commandExecute.openInventory(sender, args);
                    break;
                case "reload":
                    if (args.length == 1 && sender.hasPermission("CtOnlineReward.reload")) {
                        reload();
                        sender.sendMessage("§c§l重载成功!");
                    }
                    break;
                case "remind":
                    if (args.length == 2) {
                        String state = args[1].toLowerCase();
                        if (state.equals("on")) {
                            toggleRemind((Player) sender, true);
                            sender.sendMessage("§a§l成功打开提醒！");
                        } else if (state.equals("off")) {
                            toggleRemind((Player) sender, false);
                            sender.sendMessage("§c§l成功关闭提醒!");
                        }
                    }
                    break;
            }
        }

        return true;
    }

    private void reload() {
        ctOnlineReward.load();
        CtOnlineReward.placeholder.loadPapiJson();
    }

    private void toggleRemind(Player player, boolean enable) {
        List<Player> players = RemindTimer.players;
        if (enable) {
            players.remove(player);
            player.sendMessage("§a§l成功打开提醒！");
        } else {
            players.add(player);
            player.sendMessage("§c§l成功关闭提醒!");
        }
    }
}
