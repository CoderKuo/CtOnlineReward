package cn.ctcraft.ctonlinereward.javaw.bootstrap;

import javax.swing.*;
import java.awt.*;

public class Bootstrap {
    public static void main(String[] args) {
        System.out.println("CtOnlineReward是一个Bukkit插件");
        System.out.println("你不能直接打开它,请把它放进服务端plugins文件夹内");
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                java.net.URI uri = java.net.URI.create("https://www.mcbbs.net/thread-1166875-1-1.html");
                java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    createAndShowGUI(true);
                    dp.browse(uri);
                } else {
                    createAndShowGUI(false);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } else {
            createAndShowGUI(false);
        }
    }


    private static void createAndShowGUI(boolean supportBrowse) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        if (supportBrowse) {
            JOptionPane.showMessageDialog(new JPanel(), "<html><body><p>CtOnlineReward 是一个Bukkit插件 .</p>" +
                    "<p>你不能直接打开它，请按照<a href=\"https://www.mcbbs.net/thread-1166875-1-1.html\">帖子</a>内的步骤正确使用插件." +
                    "<p>可以通过点击 \"OK\" 按钮来打开MCBBS帖子文件.</p>" +
                    "</body></html>", "CtOnlineReward", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(new JPanel(), "<html><body><p>CtOnlineReward 是一个Bukkit插件 .</p>" +
                    "<p>你不能直接打开它，请按照<a href=\"https://www.mcbbs.net/thread-1166875-1-1.html\">帖子</a>内的步骤正确使用插件." +
                    "<p>可以通过点击 \"OK\" 按钮来打开MCBBS帖子文件.</p>" +
                    "</body></html>", "CtOnlineReward", JOptionPane.ERROR_MESSAGE);
        }
    }
}
