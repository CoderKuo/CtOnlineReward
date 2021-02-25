package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class HikariCPBase {

    private static HikariDataSource sqlConnectionPool;

    public HikariCPBase() {
        HikariConfig hikariConfig = new HikariConfig();
        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        FileConfiguration config = plugin.getConfig();
        String type = config.getString("database.type");
        ConfigurationSection databaseConfig = config.getConfigurationSection("database");
        if (type.equalsIgnoreCase("mysql")) {
            hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
            String ip = databaseConfig.getString("mysql_ip");
            String port = databaseConfig.getString("mysql_port");
            String username = databaseConfig.getString("mysql_username");
            String password = databaseConfig.getString("mysql_password");
            String database = databaseConfig.getString("mysql_database");
            String option = databaseConfig.getString("mysql_option");

            String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?" + option;
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
        } else {
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder() + "/data.db");
        }
        ConfigurationSection hikariCP = plugin.getConfig().getConfigurationSection("database.hikariCP");
        long connectionTimeout = hikariCP.getLong("connectionTimeout");
        if (connectionTimeout != 0) {
            hikariConfig.setConnectionTimeout(connectionTimeout);
        }
        int minimumIdle = hikariCP.getInt("minimumIdle");
        if (minimumIdle != 0) {
            hikariConfig.setMinimumIdle(minimumIdle);
        }
        int maximumPoolSize = hikariCP.getInt("maximumPoolSize");
        if (maximumPoolSize != 0) {
            hikariConfig.setMaximumPoolSize(maximumPoolSize);
        }
        hikariConfig.setAutoCommit(true);

        sqlConnectionPool = new HikariDataSource(hikariConfig);

    }

    public HikariDataSource getSqlConnectionPool(){
        return sqlConnectionPool;
    }

}
