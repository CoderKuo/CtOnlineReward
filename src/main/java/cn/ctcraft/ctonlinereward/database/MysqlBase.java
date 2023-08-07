package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.pojo.OnlineTimeData;
import cn.ctcraft.ctonlinereward.pojo.RewardData;
import cn.ctcraft.ctonlinereward.pojo.RewardInDatabase;
import cn.ctcraft.ctonlinereward.service.cache.CacheSystem;
import cn.ctcraft.ctonlinereward.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MysqlBase implements DataService {
    private final CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private final LinkedList<RewardInDatabase> rewards = new LinkedList<>();
    private final LinkedList<OnlineTimeData> onlineTimeData = new LinkedList<>();

    private final CacheSystem<String, Integer> onlineTimeCache = new CacheSystem<>(60);

    public MysqlBase() {
        createTable();
    }

    public Connection getConnection() throws SQLException {
        return CtOnlineReward.hikariCPBase.getSqlConnectionPool().getConnection();
    }


    public void createTable() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            String sql = "create table online_time (" +
                    "player_uuid varchar(36) NOT NULL," +
                    "login_time TIMESTAMP NOT NULL," +
                    "logout_time TIMESTAMP NOT NULL," +
                    "duration int NOT NULL ," +
                    "INDEX idx_player_uuid (player_uuid)," +
                    "INDEX idx_login_time (login_time)," +
                    "INDEX idx_logout_time (logout_time)) ENGINE = InnoDB CHARACTER SET = utf8";
            String sql2 = "CREATE TABLE rewards (" +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "reward_id VARCHAR(255) NOT NULL," +
                    "received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_player_uuid (player_uuid)," +
                    "INDEX idx_reward_id (reward_id)," +
                    "INDEX idx_received_at (received_at)" +
                    ") ENGINE = InnoDB CHARACTER SET = utf8";
            int i = statement.executeUpdate(sql);
            if (i > 0) {
                String lang = CtOnlineReward.languageHandler.getLang("mysql.createTable");
                ctOnlineReward.getLogger().info(lang);
            }
            i = statement.executeUpdate(sql2);
            if (i > 0) {
                String lang = CtOnlineReward.languageHandler.getLang("mysql.createTable");
                ctOnlineReward.getLogger().info(lang);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getPlayerOnlineTime(OfflinePlayer player) {
        if (onlineTimeCache.containsKey(player.getUniqueId().toString() + "-day")) {
            return onlineTimeCache.get(player.getUniqueId().toString() + "-day");
        }

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT" +
                     "SUM(" +
                     "CASE WHEN DATE(login_time) = DATE(logout_time) THEN duration ELSE TIMESTAMPDIFF(MINUTE, login_time, CONCAT(DATE(login_time), ' 23:59:59')) END" +
                     ") AS today_online_time" +
                     "FROM online_time" +
                     "WHERE player_uuid = ? AND DATE(login_time) = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, Util.getDateNew());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int onlineTime = rs.getInt(1);
                    onlineTimeCache.put(player.getUniqueId().toString() + "-day", onlineTime);
                    return onlineTime;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getPlayerOnlineTimeFromRange(OfflinePlayer player, Long start, Long end) {
        if (onlineTimeCache.containsKey(player.getUniqueId().toString() + "-" + start + "-" + end)) {
            return onlineTimeCache.get(player.getUniqueId().toString() + "-" + start + "-" + end);
        }

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT" +
                     "SUM(" +
                     "CASE WHEN DATE(login_time) = DATE(logout_time) THEN duration ELSE TIMESTAMPDIFF(MINUTE, login_time, CONCAT(DATE(login_time), ' 23:59:59')) END" +
                     ") AS today_online_time" +
                     "FROM online_time" +
                     "WHERE player_uuid = ? AND DATE(login_time) >=? AND DATE(logout_time) <= ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, Util.getDateNew(start));
            ps.setString(3, Util.getDateNew(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int onlineTime = rs.getInt(1);
                    onlineTimeCache.put(player.getUniqueId().toString() + "-" + start + "-" + end, onlineTime);
                    return onlineTime;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void insertPlayerOnlineTime(OfflinePlayer player, long loginTime, long logoutTime) {
        onlineTimeData.add(new OnlineTimeData(player, loginTime, logoutTime));
    }

    public void insertPlayerOnlineTimeToDataBase() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            StringBuilder sql = new StringBuilder("INSERT INTO online_time " +
                    "VALUES ");
            for (OnlineTimeData onlineTimeDatum : onlineTimeData) {
                sql.append("('" + onlineTimeDatum.getPlayer() + "','");
                sql.append(Util.getDateNew(onlineTimeDatum.getLoginTime()) + "','");
                sql.append(Util.getDateNew(onlineTimeDatum.getLogoutTime()) + "',");
                sql.append("TIMESTAMPDIFF(MINUTE, '" + Util.getDateNew(onlineTimeDatum.getLoginTime()) + "', '" + Util.getDateNew(onlineTimeDatum.getLogoutTime()) + "')),");
            }
            sql = sql.deleteCharAt(sql.length() - 2);
            int i = statement.executeUpdate(sql.toString());
            if (i < 0) {
                ctOnlineReward.getLogger().warning("§c§l■ 数据库异常，数据插入失败！");
            }
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message.contains("doesn't exist")) {
                createTable();
            } else {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<RewardInDatabase> getPlayerRewardArray(OfflinePlayer player, long start, long end) {
        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement("SELECT reward_id,received_at" +
                "FROM rewards" +
                "WHERE player_uuid = '?'" +
                " AND received_at >= ?" +
                " AND received_at <= ?")) {

            ps.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = ps.executeQuery();
            List<RewardInDatabase> list = new ArrayList<>();
            while (resultSet.next()) {
                String reward_id = resultSet.getString("reward_id");
                Timestamp rewardAt = resultSet.getTimestamp("reward_at");
                list.add(new RewardInDatabase(player.getUniqueId().toString(),reward_id,rewardAt.getTime()));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    @Override
    public boolean addRewardToPlayData(String rewardId, Player player) {
        return rewards.add(new RewardInDatabase(player.getUniqueId().toString(), rewardId, System.currentTimeMillis()));
    }


    @Override
    public int getPlayerOnlineTimeWeek(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();

        if (onlineTimeCache.containsKey(uuid + "-week")) {
            return onlineTimeCache.get(uuid + "-week");
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT SUM(duration) AS total_online_time" +
                    "FROM online_time" +
                    "WHERE player_uuid = ?" +
                    "    AND WEEK(login_time, 1) = WEEK(CURDATE(), 1)" +
                    "GROUP BY player_uuid");
            ps.setString(1, uuid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int weekOnlineTime = rs.getInt("total_online_time");
                onlineTimeCache.put(uuid + "-week", weekOnlineTime);
                return weekOnlineTime;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    @Override
    public int getPlayerOnlineTimeMonth(OfflinePlayer player) {


        String uuid = player.getUniqueId().toString();
        if (onlineTimeCache.containsKey(uuid + "-month")) {
            return onlineTimeCache.get(uuid + "-month");
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT player_uuid, SUM(duration) AS total_online_time" +
                    "FROM online_time" +
                    "WHERE player_uuid = ?" +
                    "    AND YEAR(login_time) = YEAR(CURDATE())" +
                    "    AND MONTH(login_time) = MONTH(CURDATE())" +
                    "GROUP BY player_uuid");
            ps.setString(1, uuid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int monthOnlineTime = rs.getInt("total_online_time");
                onlineTimeCache.put(uuid + "-month", monthOnlineTime);
                return monthOnlineTime;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    @Override
    public int getPlayerOnlineTimeAll(OfflinePlayer player) {


        String uuid = player.getUniqueId().toString();
        if (onlineTimeCache.containsKey(uuid + "-all")) {
            return onlineTimeCache.get(uuid + "-all");
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT player_uuid, SUM(duration) AS total_online_time" +
                    "FROM online_time" +
                    "WHERE player_uuid = ?" +
                    "GROUP BY player_uuid");
            ps.setString(1, uuid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int allOnlineTime = rs.getInt("total_online_time");
                onlineTimeCache.put(uuid + "-all", allOnlineTime);
                return allOnlineTime;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }


    @Override
    public void flush() {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(ctOnlineReward, new Runnable() {
                @Override
                public void run() {
                    insertPlayerOnlineTimeToDataBase();
                    onlineTimeCache.clear();
                }
            });
        } else {
            insertPlayerOnlineTimeToDataBase();
            onlineTimeCache.clear();
        }
    }
}
