package cn.ctcraft.ctonlinereward.service.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheSystem<K, V> {
    private final Map<K, V> cacheMap;
    private final long cacheExpirationTime;
    private final ScheduledExecutorService executor;

    public CacheSystem(long cacheExpirationTimeInSeconds) {
        this.cacheMap = new ConcurrentHashMap<>();
        this.cacheExpirationTime = cacheExpirationTimeInSeconds * 1000; // 转换为毫秒
        this.executor = Executors.newScheduledThreadPool(1);
    }

    public void put(K key, V value) {
        cacheMap.put(key, value);
        scheduleCacheExpiration(key);
    }

    public V get(K key) {
        return cacheMap.get(key);
    }

    public void remove(K key) {
        cacheMap.remove(key);
    }

    private void scheduleCacheExpiration(K key) {
        executor.schedule(() -> remove(key), cacheExpirationTime, TimeUnit.MILLISECONDS);
    }

    public void clear() {
        cacheMap.clear();
    }

    public int size() {
        return cacheMap.size();
    }

    public boolean containsKey(K key) {
        return cacheMap.containsKey(key);
    }
}
