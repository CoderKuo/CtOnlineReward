package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.utils.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLiteBase implements DataService {
    private CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);

    public SQLiteBase(){
        createTable();
    }

    public Connection getConnection() throws SQLException {
        return CtOnlineReward.hikariCPBase.getSqlConnectionPool().getConnection();
    }


    public void createTable() {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();
            String date = Util.getDate();
            String sql = "CREATE TABLE IF NOT EXISTS `"+date+"`  (" +
                    "  `uuid` varchar(255) NOT NULL," +
                    "  `online_data` varchar(255) DEFAULT NULL," +
                    "  PRIMARY KEY (`uuid`) " +
                    ")";
            ps = connection.prepareStatement(sql);
            int i = ps.executeUpdate();
            if (i > 0) {
                String lang = CtOnlineReward.languageHandler.getLang("mysql.createTable");
                ctOnlineReward.getLogger().info(lang);
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
        }
    }

    @Override
    public int getPlayerOnlineTime(Player pLayer) {
        JsonObject playerOnlineData = getPlayerOnlineData(pLayer);
        JsonElement time = playerOnlineData.get("time");
        if (time == null){
            insertPlayerOnlineTime(pLayer,0);
            return 0;
        }
        return time.getAsInt();
    }

    @Override
    public void addPlayerOnlineTime(Player player, int time) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();
            String date = Util.getDate();
            String sql = "update `"+date+"` set `online_data` = ? where `uuid` = ?";
            ps = connection.prepareStatement(sql);
            JsonObject playerOnlineData = getPlayerOnlineData(player);
            playerOnlineData.addProperty("time", time);
            String asString = playerOnlineData.toString();
            ps.setString(1, asString);
            ps.setString(2,player.getUniqueId().toString());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public JsonObject getPlayerOnlineData(Player player) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            String date = Util.getDate();
            String sql = "select `online_data` from `"+date+"` where `uuid`=?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, player.getUniqueId().toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                JsonParser jsonParser = new JsonParser();
                JsonElement parse = jsonParser.parse(rs.getString(1));
                if (parse.isJsonNull()) {
                    return new JsonObject();
                }
                return parse.getAsJsonObject();
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
        return new JsonObject();
    }

    @Override
    public void insertPlayerOnlineTime(Player player,int time) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();
            String date = Util.getDate();
            String sql = "insert into `"+date+"` (`uuid`,`online_data`) values (?,?)";
            ps = connection.prepareStatement(sql);
            ps.setString(1, player.getUniqueId().toString());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("time", time);
            jsonObject.add("reward", new JsonArray());
            ps.setString(2, jsonObject.toString());
            int i = ps.executeUpdate();
            if (i < 0) {
                ctOnlineReward.getLogger().warning("§c§l■ 数据库异常，数据插入失败！");
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains("doesn't exist")){
                createTable();
            }else {
                e.printStackTrace();
            }
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
        }
    }

    @Override
    public List<String> getPlayerRewardArray(Player player) {
        JsonObject playerOnlineData = getPlayerOnlineData(player);
        JsonElement reward = playerOnlineData.get("reward");
        List<String> rewardList = new ArrayList<>();
        if (reward == null) {
            return rewardList;
        }
        JsonArray asJsonArray = reward.getAsJsonArray();
        for (JsonElement jsonElement : asJsonArray) {
            rewardList.add(jsonElement.getAsString());
        }
        return rewardList;
    }

    @Override
    public boolean addRewardToPlayData(String rewardId, Player player) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            JsonObject playerOnlineData = getPlayerOnlineData(player);
            JsonElement reward = playerOnlineData.get("reward");
            if (reward == null) {
                playerOnlineData.add("reward", new JsonArray());
                reward = playerOnlineData.get("reward");
            }
            JsonArray asJsonArray = reward.getAsJsonArray();
            asJsonArray.add(rewardId);
            playerOnlineData.add("reward", asJsonArray);
            connection = getConnection();
            String date = Util.getDate();
            String sql = "update `"+date+"` set `online_data` = ? where `uuid` = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, playerOnlineData.toString());
            ps.setString(2, player.getUniqueId().toString());
            int i = ps.executeUpdate();
            if (i > 0) {
                return true;
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
        }
        return false;
    }

    @Override
    public int getPlayerOnlineTimeWeek(Player player) {
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
    public int getPlayerOnlineTimeMonth(Player player) {
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
    public int getPlayerOnlineTimeAll(Player player) {
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
