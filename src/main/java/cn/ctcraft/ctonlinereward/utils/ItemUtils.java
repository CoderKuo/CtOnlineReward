package cn.ctcraft.ctonlinereward.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class ItemUtils {
    public static ItemStack crearSkull(String textura) {
        ItemStack item = null;
        if (esLegacy()) {
            item = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short)3);
        } else {
            item = new ItemStack(Material.valueOf("PLAYER_HEAD"));
        }
        if (textura.isEmpty())
            return item;
        SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", textura));
        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException|NoSuchFieldException|SecurityException|IllegalAccessException error) {
            error.printStackTrace();
        }
        item.setItemMeta((ItemMeta)skullMeta);
        return item;
    }

    public static boolean esLegacy() {
        if (Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") ||
                Bukkit.getVersion().contains("1.15") || Bukkit.getVersion().contains("1.16") ||
                Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18") ||
                Bukkit.getVersion().contains("1.19") || Bukkit.getVersion().contains("1.20"))
            return false;
        return true;
    }
}
