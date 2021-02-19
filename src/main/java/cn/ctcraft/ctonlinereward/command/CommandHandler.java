package cn.ctcraft.ctonlinereward.command;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
    private static CommandHandler instance = new CommandHandler();
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private CommandExecute commandExecute = CommandExecute.getInstance();
    public static CommandHandler getInstance(){
        return instance;
    }
    private CommandHandler(){

    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!label.equalsIgnoreCase("cor")){
            return true;
        }
        if(args.length == 0){
            if(sender instanceof Player){
                if (!sender.hasPermission("CtOnlineReward.cor")){
                    sender.sendMessage("§c§l权限不足!");
                }
                commandExecute.openInventory(sender,new String[]{"1"});

            }else {
                sender.sendMessage(ctOnlineReward.getDescription().getName());
                sender.sendMessage(ctOnlineReward.getDescription().getVersion());

            }
        }
        if(args.length > 0){
            if(args[0].equalsIgnoreCase("reward")){
                commandExecute.openRewardSetInventory(sender,args);

            }
            if(args[0].equalsIgnoreCase("open")){
                commandExecute.openInventory(sender,args);
            }
            if(args[0].equalsIgnoreCase("reload") && args.length == 1 && sender.hasPermission("CtOnlineReward.reload")){
                reload();
                sender.sendMessage("§c§l重载成功!");
            }
        }


        return true;
    }

    private void reload(){
        ctOnlineReward.load();
    }
}
