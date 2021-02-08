package cn.ctcraft.ctonlinereward;

import cn.ctcraft.ctonlinereward.command.CommandHandler;
import cn.ctcraft.ctonlinereward.database.YamlData;
import cn.ctcraft.ctonlinereward.inventory.RewardSetInventoryMonitor;
import cn.ctcraft.ctonlinereward.listner.InventoryMonitor;
import cn.ctcraft.ctonlinereward.service.OnlineTimer;
import cn.ctcraft.ctonlinereward.service.YamlService;
import cn.ctcraft.ctonlinereward.utils.version;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public final class CtOnlineReward extends JavaPlugin {
    public static Economy economy = null;

    @Override
    public void onEnable() {
        Logger logger = getLogger();

        this.getCommand("cor").setExecutor(CommandHandler.getInstance());
        getServer().getPluginManager().registerEvents(RewardSetInventoryMonitor.getInstance(), this);
        getServer().getPluginManager().registerEvents(new InventoryMonitor(), this);

        load();

        OnlineTimer.getInstance().runTaskTimerAsynchronously(this,1200,1200);

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            Placeholder.getInstance().register();
        }else{
            getLogger().warning("§e§l未找到PlaceholderAPI.");
        }

        if(Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")){
            getLogger().info("§e§l获取PlayerPoints成功!");
        }

        RegisteredServiceProvider<Economy> economyProvider =  getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }else {
            getLogger().warning("§e§l初始化Vault失败.");
        }

        BukkitTask versionTask = new BukkitRunnable() {
            @Override
            public void run() {
                List<String> versionMsg = version.getVersionMsg();
                versionMsg.forEach(System.out::println);
            }
        }.runTaskAsynchronously(this);

        Metrics metrics = new Metrics(this);

        logger.info("§a§l● 在线奖励加载成功!");


    }

    public void load(){
        saveDefaultConfig();

        File file = new File(getDataFolder() + "/rewardData/");

        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        Logger logger = plugin.getLogger();
        if (!file.exists()) {
            boolean mkdir = file.mkdir();
            if (mkdir) {
                saveResource("rewardData/10min",false);
                logger.info("§a§l● 奖励目录构建完成!");
            } else {
                logger.warning("§c§l■ 奖励目录构建失败,插件即将关闭!");
                getPluginLoader().disablePlugin(this);
            }
        }


        YamlService yamlService = YamlService.getInstance();
        try {
            boolean b = yamlService.loadGuiYaml();
            if (b) {
                logger.info("§a§l● Gui配置文件加载成功,共加载" + YamlData.guiYaml.size() + "个配置文件!");
            }
        }catch (Exception e){
            logger.info("§c§l■ Gui配置文件加载失败!");

            e.printStackTrace();
        }

        boolean b1 = yamlService.loadPlayerDataYaml();
        if(b1){
            logger.info("§a§l● 玩家配置文件加载成功!");
        }
        boolean b2 = yamlService.loadRewardYaml();
        if(b2){
            logger.info("§a§l● 奖励配置文件加载成功!");
        }

    }

    @Override
    public void onDisable() {

    }

    public PlayerPoints getPlayerPoints(){return (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");}


}
