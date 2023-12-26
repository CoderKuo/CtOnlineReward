package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.utils.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MysqlBase implements DataService {
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    public MysqlBase() {
        createTable();
    }

    public Connection getConnection() throws SQLException {
        return CtOnlineReward.hikariCPBase.getSqlConnectionPool().getConnection();
    }


    public void createTable() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            String date = Util.getDate();
            String sql = "CREATE TABLE IF NOT EXISTS `" + date + "`  (" +
                    "  `uuid` varchar(255) NOT NULL COMMENT '玩家uuid'," +
                    "  `online_data` varchar(255) DEFAULT NULL COMMENT '在线数据'," +
                    "  PRIMARY KEY (`uuid`) " +
                    ") ENGINE = InnoDB CHARACTER SET = utf8";
            int i = statement.executeUpdate(sql);
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
        JsonObject playerOnlineData = getPlayerOnlineData(player);
        JsonElement time = playerOnlineData.get("time");
        int onlineTime = time != null ? time.getAsInt() : -1;

        if (onlineTime == -1) {
            insertPlayerOnlineTime(player, 0);
        }

        return onlineTime;
    }

    @Override
    public void addPlayerOnlineTime(OfflinePlayer player, int time) {
        String date = Util.getDate();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE `"+date+"` SET `online_data` = ? WHERE `uuid` = ?")) {
            JsonObject playerOnlineData = getPlayerOnlineData(player);
            playerOnlineData.addProperty("time", time);

            ps.setString(1, playerOnlineData.toString());
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public JsonObject getPlayerOnlineData(OfflinePlayer player) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT `online_data` FROM `"+Util.getDate()+"` WHERE `uuid` = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String onlineData = rs.getString(1);
                    if (onlineData != null && !onlineData.isEmpty()) {
                        JsonParser jsonParser = new JsonParser();
                        JsonElement parse = jsonParser.parse(onlineData);
                        if (!parse.isJsonNull()) {
                            return parse.getAsJsonObject();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JsonObject();
    }

    @Override
    public void insertPlayerOnlineTime(OfflinePlayer player, int time) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO `"+Util.getDate()+"` (`uuid`, `online_data`) VALUES (?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("time", time);
            jsonObject.add("reward", new JsonArray());
            ps.setString(2, jsonObject.toString());
            int i = ps.executeUpdate();
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
    public List<String> getPlayerRewardArray(OfflinePlayer player) {
        JsonObject playerOnlineData = getPlayerOnlineData(player);
        JsonElement reward = playerOnlineData.get("reward");
        List<String> rewardList = new ArrayList<>();
        if (reward != null && reward.isJsonArray()) {
            JsonArray jsonArray = reward.getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                rewardList.add(jsonElement.getAsString());
            }
        }
        return rewardList;
    }


    @Override
    public boolean addRewardToPlayData(String rewardId, Player player) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE `"+Util.getDate()+"` SET `online_data` = ? WHERE `uuid` = ?")) {
            JsonObject playerOnlineData = getPlayerOnlineData(player);
            JsonElement reward = playerOnlineData.get("reward");
            if (reward == null) {
                playerOnlineData.add("reward", new JsonArray());
                reward = playerOnlineData.get("reward");
            }
            if (reward.isJsonArray()) {
                JsonArray rewardArray = reward.getAsJsonArray();
                rewardArray.add(rewardId);
            }
            ps.setString(1, playerOnlineData.toString());
            ps.setString(2, player.getUniqueId().toString());
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public int getPlayerOnlineTimeWeek(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int onlineTime = 0;
        try {
            connection = getConnection();
            SqlUtil sqlUtil = SqlUtil.getInstance();
            List<String> tableList = sqlUtil.getTableList();
            List<String> weekString = Util.getWeekString();
            String sql = "";
            for (String s : weekString) {
                if (tableList.contains(s)) {
                    if (sql.equalsIgnoreCase("")) {
                        sql = "select `online_data` from `" + s + "` where uuid='" + uuid + "'";
                    } else {
                        sql = sql.concat(" union all select `online_data` from `" + s + "` where uuid='" + uuid + "'");
                    }
                }
            }
            if (StringUtils.isEmpty(sql)){
                return onlineTime;
            }
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                JsonParser jsonParser = new JsonParser();
                JsonElement parse = jsonParser.parse(rs.getString(1));
                if (!parse.isJsonNull()) {
                    int onlineTimeByJsonObject = Util.getOnlineTimeByJsonObject(parse.getAsJsonObject());
                    onlineTime += onlineTimeByJsonObject;
                }
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
        return onlineTime;
    }

    @Override
    public int getPlayerOnlineTimeMonth(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int onlineTime = 0;
        try {
            connection = getConnection();
            SqlUtil sqlUtil = SqlUtil.getInstance();
            List<String> tableList = sqlUtil.getTableList();
            List<String> monthString = Util.getMonthString();
            String sql = "";
            for (String s : monthString) {
                if (tableList.contains(s)) {
                    if (sql.equalsIgnoreCase("")) {
                        sql = "select `online_data` from `" + s + "` where uuid='" + uuid + "'";
                    } else {
                        sql = sql.concat(" union all select `online_data` from `" + s + "` where uuid='" + uuid + "'");
                    }
                }
            }
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                JsonParser jsonParser = new JsonParser();
                JsonElement parse = jsonParser.parse(rs.getString(1));
                if (!parse.isJsonNull()) {
                    int onlineTimeByJsonObject = Util.getOnlineTimeByJsonObject(parse.getAsJsonObject());
                    onlineTime += onlineTimeByJsonObject;
                }
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
        return onlineTime;
    }

    @Override
    public int getPlayerOnlineTimeAll(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int onlineTime = 0;
        try {
            connection = getConnection();
            SqlUtil sqlUtil = SqlUtil.getInstance();
            List<String> tableList = sqlUtil.getTableList();
            String sql = "";
            for (String s : tableList) {
                if (sql.equalsIgnoreCase("")) {
                    sql = "select `online_data` from `" + s + "` where uuid='" + uuid + "'";
                } else {
                    sql = sql.concat(" union all select `online_data` from `" + s + "` where uuid='" + uuid + "'");
                }
            }
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                JsonParser jsonParser = new JsonParser();
                JsonElement parse = jsonParser.parse(rs.getString(1));
                if (!parse.isJsonNull()) {
                    int onlineTimeByJsonObject = Util.getOnlineTimeByJsonObject(parse.getAsJsonObject());
                    onlineTime += onlineTimeByJsonObject;
                }
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
        return onlineTime;
    }
}
