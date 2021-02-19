package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlUtil {
    private HikariDataSource sqlConnectionPool = CtOnlineReward.hikariCPBase.getSqlConnectionPool();
    private static SqlUtil instance = new SqlUtil();
    private SqlUtil(){}

    public static SqlUtil getInstance() {
        return instance;
    }

    public boolean hasTable(String tableName){
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = sqlConnectionPool.getConnection();
            statement = connection.createStatement();
            DatabaseMetaData md = statement.getConnection().getMetaData();
            rs = md.getTables((String) null, (String) null, tableName, (String[]) null);
            while (rs.next()){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null){
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public List<String> getTableList(){
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<>();
        try {
            connection = sqlConnectionPool.getConnection();
            statement = connection.createStatement();
            DatabaseMetaData md = statement.getConnection().getMetaData();
            CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
            FileConfiguration config = plugin.getConfig();
            rs = md.getTables((String) null,config.getString("database.mysql_database"), (String)null, (String[]) null);
            while (rs.next()){
                list.add(rs.getString("TABLE_NAME"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null){
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }
}
