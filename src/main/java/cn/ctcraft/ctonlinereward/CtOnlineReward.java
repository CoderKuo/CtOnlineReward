package cn.ctcraft.ctonlinereward;

import cn.ctcraft.ctonlinereward.command.CommandHandler;
import cn.ctcraft.ctonlinereward.database.*;
import cn.ctcraft.ctonlinereward.inventory.RewardSetInventoryMonitor;
import cn.ctcraft.ctonlinereward.listner.InventoryMonitor;
import cn.ctcraft.ctonlinereward.listner.PlayerMonitor;
import cn.ctcraft.ctonlinereward.service.RewardService;
import cn.ctcraft.ctonlinereward.service.YamlService;
import cn.ctcraft.ctonlinereward.service.afk.AfkService;
import cn.ctcraft.ctonlinereward.service.afk.AfkTimer;
import cn.ctcraft.ctonlinereward.service.scheduler.OnlineTimer;
import cn.ctcraft.ctonlinereward.service.scheduler.RemindTimer;
import cn.ctcraft.ctonlinereward.utils.version;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public final class CtOnlineReward extends JavaPlugin {
    public static Economy economy = null;
    public static DataService dataService;
    public static Placeholder placeholder;
    public static HikariCPBase hikariCPBase;
    public static YamlConfiguration lang;
    public static LanguageHandler languageHandler;
    public static YamlConfiguration placeholderYaml;


    @Override
    public void onEnable() {
        final long timestamp = System.currentTimeMillis();

        this.getCommand("cor").setExecutor(CommandHandler.getInstance());
        getServer().getPluginManager().registerEvents(RewardSetInventoryMonitor.getInstance(), this);
        getServer().getPluginManager().registerEvents(new InventoryMonitor(), this);
        getServer().getPluginManager().registerEvents(new PlayerMonitor(), this);

        load();


        BukkitTask versionTask = new BukkitRunnable() {
            @Override
            public void run() {
                List<String> versionMsg = version.getVersionMsg();
                versionMsg.forEach(getLogger()::info);
            }
        }.runTaskAsynchronously(this);


        Metrics metrics = new Metrics(this);

        String databaseType = getConfig().getString("database.type");
        if (databaseType.equalsIgnoreCase("yaml")) {
            dataService = new YamlBase();
        } else if (databaseType.equalsIgnoreCase("mysql")) {
            hikariCPBase = new HikariCPBase();
            dataService = new MysqlBase();
        } else if (databaseType.equalsIgnoreCase("SQLite")) {
            hikariCPBase = new HikariCPBase();
            dataService = new SQLiteBase();
        } else {
            dataService = new YamlBase();
        }


        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholder = new Placeholder();
            placeholder.register();
        } else {
            getLogger().warning("§e§l未找到PlaceholderAPI.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            getLogger().info("§e§l获取PlayerPoints成功!");
        }

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        } else {
            getLogger().warning("§e§l初始化Vault失败.");
        }

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        try {
            yamlConfiguration.load(getDataFolder() + "/lang.yml");
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("§c§l■ lang.yml文件加载失败!", e);
        }
        lang = yamlConfiguration;


        languageHandler = new LanguageHandler();


        OnlineTimer.getInstance().runTaskTimerAsynchronously(this, 1200, 1200);

        boolean afk = getConfig().getBoolean("Setting.afkConfig.use");
        if (afk) {
            int time = getConfig().getInt("Setting.afkConfig.time");

            String string = getConfig().getString("Setting.afkConfig.mode");
            if (string.equalsIgnoreCase("strong")) {
                AfkService.getInstance().openStrongMode();
            }
            new AfkTimer().runTaskTimerAsynchronously(this, 0, time * 60 * 20);
        }
        boolean onlineRemind = getConfig().getBoolean("Setting.remind.use");
        if (onlineRemind) {
            int anInt = getConfig().getInt("Setting.remind.time");
            new RemindTimer().runTaskTimerAsynchronously(this, anInt * 60 * 20, anInt * 60 * 20);
        }

        boolean useTimeLimit = getConfig().getBoolean("Setting.timeLimit.use");
        if (useTimeLimit){
            Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE+"["+ChatColor.GREEN+"在线奖励"+ChatColor.WHITE+"]"+ChatColor.GREEN+" > "+ChatColor.WHITE+ "在线时间限制已开启!"+ChatColor.WHITE+".");
            String limit = getConfig().getString("Setting.timeLimit.limit");
            String[] s = limit.split(",");
            YamlData.timeLimit[0] = Integer.parseInt(s[0]);
            YamlData.timeLimit[1] = Integer.parseInt(s[1]);
        }


        final long time = System.currentTimeMillis() - timestamp;
        Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE+"["+ChatColor.GREEN+"在线奖励"+ChatColor.WHITE+"]"+ChatColor.GREEN+" > "+ChatColor.WHITE+ "插件加载完成,共耗时"+ChatColor.GREEN+ time + "ms"+ChatColor.WHITE+".");
    }

    public void load() {

        saveDefaultConfig();

        File file = new File(getDataFolder() + "/rewardData/");

        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        Logger logger = plugin.getLogger();
        if (!file.exists()) {
            boolean mkdir = file.mkdir();
            if (mkdir) {
                boolean b = RewardService.getInstance().initRewardFile();
                if (b) {
                    logger.info("§a§l● 初次启动,奖励目录构建完成!");
                }
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
        } catch (Exception e) {
            logger.warning("§c§l■ Gui配置文件加载失败!");

            e.printStackTrace();
        }

        File langFile = new File(getDataFolder() + "/lang.yml");
        if (!langFile.exists()) {
            saveResource("lang.yml", false);
        }

        boolean b2 = yamlService.loadRewardYaml();
        if (b2) {
            logger.info("§a§l● 奖励配置文件加载成功!");
        }

        File placeholderFile = new File(getDataFolder() + "/placeholder.yml");
        if (!placeholderFile.exists()){
            saveResource("placeholder.yml",false);
        }
        placeholderYaml = new YamlConfiguration();
        try {
            placeholderYaml.load(placeholderFile);
            logger.info("§a§l● papi配置文件加载成功!");

        } catch (Exception e) {
            logger.warning("§c§l■ papi变量配置文件加载失败!");
            e.printStackTrace();
        }

        RewardService.getInstance().registerRewardCondition();

    }



    @Override
    public void onDisable() {
    }

    public PlayerPoints getPlayerPoints() {
        return (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
    }


}
