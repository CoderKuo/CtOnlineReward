package cn.ctcraft.ctonlinereward.service;


import cn.ctcraft.ctonlinereward.CtOnlineReward;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

public class LibraryLoader {

    public static void downloadLibrary(String groupId, String artifactId) {
        downloadLibrary(groupId, artifactId, getVersion(artifactId));
    }

    public static void downloadLibrary(String groupId, String artifactId, String version) {
        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                File file = new File(plugin.getDataFolder(), "libraries");
                if (file.exists()) {
                    file.mkdir();
                }
                System.out.println("§f[在线奖励] " + artifactId + " loading...");
                String jarFileName = artifactId + "-" + version + ".jar";
                String jarUrl = String.format("https://maven.aliyun.com/repository/public/%s/%s/%s/%s", groupId.replace('.', '/'), artifactId, version, jarFileName);
                try {
                    downloadJar(jarUrl, file + jarFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 加载jar包到内存中
                try {
                    File jarFile = new File(file + jarFileName);
                    URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, LibraryLoader.class.getClassLoader());
                    System.out.println("§f[在线奖励] " + artifactId + " loaded.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void downloadJar(String jarUrl, String filePath) throws IOException {
        URL url = new URL(jarUrl);
        try (InputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
    }

    private static String getVersion(String key) {
        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        File file = new File(plugin.getDataFolder(), "library.yml");
        if (file.exists()) {

            try {
                file.createNewFile();
                createDefaultFile();
                return getVersion(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        return yamlConfiguration.getString(key);
    }

    public static void createDefaultFile() throws IOException {
        CtOnlineReward plugin = CtOnlineReward.getPlugin(CtOnlineReward.class);
        File file = new File(plugin.getDataFolder(), "library.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.set("fastjson.group", "com.alibaba");
        yamlConfiguration.set("fastjson.artifact", "fastjson");
        yamlConfiguration.set("fastjson.version", "2.0.37");
        yamlConfiguration.save(file);
    }

}
