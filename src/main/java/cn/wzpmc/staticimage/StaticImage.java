package cn.wzpmc.staticimage;

import cn.wzpmc.staticimage.commands.StaticImageCommand;
import cn.wzpmc.staticimage.events.LoadChunk;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public final class StaticImage extends JavaPlugin {
    private Logger logger;
    private static NamespacedKey tagKey;
    private static NamespacedKey xywhKey;

    @Override
    public void onEnable() {
        logger = this.getSLF4JLogger();
        tagKey = NamespacedKey.fromString("image", this);
        xywhKey = NamespacedKey.fromString("xywh", this);
        PluginCommand staticImageCommand = super.getCommand("staticimage");
        if (staticImageCommand == null){
            logger.error("无法注册staticimage指令");
            return;
        }
        StaticImageCommand commandInstance = new StaticImageCommand();
        staticImageCommand.setExecutor(commandInstance);
        staticImageCommand.setTabCompleter(commandInstance);
        Server server = super.getServer();
        PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(new LoadChunk(), this);
        logger.info("启用StaticImage插件成功！");
    }

    @Override
    public void onDisable() {
        logger.info("禁用StaticImage插件成功！");
    }
    public static NamespacedKey getTagKey(){
        return StaticImage.tagKey;
    }
    public static NamespacedKey getXywhKey(){
        return StaticImage.xywhKey;
    }
}
