package cn.ctcraft.ctonlinereward.database;

import cn.ctcraft.ctonlinereward.CtOnlineReward;
import cn.ctcraft.ctonlinereward.pojo.OnlineTimeData;
import cn.ctcraft.ctonlinereward.pojo.RewardInDatabase;
import cn.ctcraft.ctonlinereward.utils.ConfigUtil;
import cn.ctcraft.ctonlinereward.utils.Util;
import io.netty.util.internal.ConcurrentSet;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class YamlBase implements DataService {
    private final CtOnlineReward ctOnlineReward = CtOnlineReward.getPlugin(CtOnlineReward.class);
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final ConcurrentHashMap<String, YamlConfiguration> playerYamlCache = new ConcurrentHashMap<>();
    private final Set<String> dirty = new ConcurrentSet<>();

    public YamlConfiguration loadYamlFromUUID(String uuid) {
        if (playerYamlCache.containsKey(uuid)) {
            return playerYamlCache.get(uuid);
        }
        readLock.lock();
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(getOnlineTimeFile(uuid));
            playerYamlCache.put(uuid, yaml);
        } catch (Exception e) {
            ctOnlineReward.getLogger().warning("数据文件读取失败!");
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
        return yaml;
    }

    private File getOnlineTimeFile(String uuid) {
        File fileDir = new File(ctOnlineReward.getDataFolder(), "/player_data");
        File file = new File(fileDir, uuid + ".yml");
        if (!fileDir.exists()) {
            fileDir.mkdir();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public List<OnlineTimeData> getPlayerOnlineDataFromYaml(OfflinePlayer player) {
        YamlConfiguration yamlConfiguration = loadYamlFromUUID(player.getUniqueId().toString());
        try {
            return ConfigUtil.getObjectList(yamlConfiguration, "online_time", OnlineTimeData.class);
        } catch (Exception e) {
            ctOnlineReward.getLogger().warning("玩家数据解析失败，请检查是否手动设置出错。如仍无法解决请联系作者解决");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int getPlayerOnlineTimeFromRange(OfflinePlayer player, Long start, Long end) {
        List<OnlineTimeData> playerOnlineDataFromYaml = getPlayerOnlineDataFromYaml(player);
        return playerOnlineDataFromYaml.stream().filter(onlineTimeData -> {
            return (onlineTimeData.getLoginTime() > start && onlineTimeData.getLogoutTime() < end);
        }).map(onlineTimeData -> Util.timeDiff(onlineTimeData.getLoginTime(), onlineTimeData.getLogoutTime())).map((Function<String, Integer>) Integer::parseInt).reduce(0, Integer::sum);
    }

    public static LocalDateTime fromTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    public int getPlayerOnlineTime(OfflinePlayer pLayer) {
        return getPlayerOnlineTimeFromRange(pLayer, Util.getStartOfToday(), Util.getEndOfToday());
    }


    public void addPlayerOnlineTime(OfflinePlayer player, OnlineTimeData... data) {
        YamlConfiguration yamlConfiguration = loadYamlFromUUID(player.getUniqueId().toString());
        List<Map<?, ?>> simpleEntries = new ArrayList<>();
        Arrays.stream(data).forEach(onlineTimeData -> {
            Map<String, String> map = new HashMap<>();
            map.put("login_time", Util.getDateNew(onlineTimeData.getLoginTime()));
            map.put("logout", Util.getDateNew(onlineTimeData.getLogoutTime()));
            map.put("duration", Util.timeDiff(onlineTimeData.getLoginTime(), onlineTimeData.getLogoutTime()));
            simpleEntries.add((Map<?, ?>) map);
        });
        ConfigUtil.insert(yamlConfiguration, "online_time", simpleEntries);
        setDirty(player);
    }

    private void setDirty(OfflinePlayer player) {
        dirty.add(player.getUniqueId().toString());
    }

    @Override
    public void insertPlayerOnlineTime(OfflinePlayer player, long loginTime, long logoutTime) {
        addPlayerOnlineTime(player, new OnlineTimeData(player, loginTime, logoutTime));
    }

    public List<String> getPlayerRewardArray(OfflinePlayer player, long start, long end) {
        YamlConfiguration yamlConfiguration = loadYamlFromUUID(player.getUniqueId().toString());
        try {
            List<RewardInDatabase> rewards = ConfigUtil.getObjectList(yamlConfiguration, "rewards", RewardInDatabase.class);
            return rewards.stream().filter(rewardInDatabase -> {
                return (rewardInDatabase.getReceived_at() >= start && rewardInDatabase.getReceived_at() <= end);
            }).map((Function<RewardInDatabase, String>) RewardInDatabase::getReward_Id).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 添加领取奖励记到数据
     *
     * @param rewardId 奖励ID
     * @param player   玩家
     * @return 是否添加成功
     */
    public boolean addRewardToPlayData(String rewardId, Player player) {
        YamlConfiguration yamlConfiguration = loadYamlFromUUID(player.getUniqueId().toString());
        Map<String, String> map = new HashMap<>();
        map.put("reward_id", rewardId);
        map.put("received_at", Util.getDateNew(System.currentTimeMillis()));
        ConfigUtil.insert(yamlConfiguration, "rewards", (List<Map<?, ?>>) map);
        setDirty(player);
        return true;
    }


    @Override
    public int getPlayerOnlineTimeWeek(OfflinePlayer player) {
        return getPlayerOnlineTimeFromRange(player, Util.getStartOfCurrentWeek(), Util.getEndOfCurrentWeek());
    }

    @Override
    public int getPlayerOnlineTimeMonth(OfflinePlayer player) {
        return getPlayerOnlineTimeFromRange(player, Util.getStartOfCurrentMonth(), Util.getEndOfCurrentMonth());
    }

    @Override
    public int getPlayerOnlineTimeAll(OfflinePlayer player) {
        return getPlayerOnlineDataFromYaml(player).stream().map(onlineTimeData -> Util.timeDiff(onlineTimeData.getLoginTime(), onlineTimeData.getLogoutTime())).map((Function<String, Integer>) Integer::parseInt).reduce(0, Integer::sum);
    }

    public void save(String uuid, YamlConfiguration yaml) {
        writeLock.lock();
        try {
            yaml.save(getOnlineTimeFile(uuid));
        } catch (Exception e) {
            ctOnlineReward.getLogger().warning("文件保存失败,请检查权限问题或联系作者!");
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        if (!Bukkit.getOfflinePlayer(UUID.fromString(uuid)).isOnline()) {
            playerYamlCache.remove(uuid);
        }
        dirty.remove(uuid);
    }


    @Override
    public void flush() {
        Iterator<String> iterator = dirty.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (!playerYamlCache.containsKey(next)) {
                iterator.remove();
                continue;
            }
            YamlConfiguration yamlConfiguration = playerYamlCache.get(next);
            Bukkit.getServer().getScheduler().runTaskAsynchronously(ctOnlineReward, new Runnable() {
                @Override
                public void run() {
                    save(next, yamlConfiguration);
                }
            });

        }
    }

}

