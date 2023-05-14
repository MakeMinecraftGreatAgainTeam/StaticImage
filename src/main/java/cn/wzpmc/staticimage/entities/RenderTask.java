package cn.wzpmc.staticimage.entities;

import cn.wzpmc.staticimage.StaticImage;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author wzp
 * @version 1.0
 * @since 2023/5/14 0:30:24
 */
public class RenderTask implements Runnable {
    protected final ItemFrame frame;
    protected final StaticImage plugin;
    protected final Logger logger;
    protected final Server server;
    protected final World world;
    protected final String imageName;
    protected final int[] xywh;
    public RenderTask(ItemFrame itemFrame, StaticImage plugin, World world, String imageName, int[] xywh){
        this.frame = itemFrame;
        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
        this.server = plugin.getServer();
        this.world = world;
        this.imageName = imageName;
        this.xywh = xywh;
    }

    @Override
    public void run() {
        File file = new File(this.plugin.getDataFolder(), imageName + ".png");
        if (!file.exists()){
            file = new File(this.plugin.getDataFolder(), imageName + ".jpg");
        }
        if (!file.exists()){
            logger.error("无法读取图片数据，图片：{}", imageName);
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedImage image = ImageIO.read(fileInputStream);
            fileInputStream.close();
            plugin.getRenderTaskRunner().addTask(new ImagedRenderTask(frame, plugin, world, imageName, xywh, image.getSubimage(xywh[0], xywh[1], xywh[2], xywh[3])));
        } catch (IOException e) {
            logger.error("无法读取图片数据，图片：{}", imageName);
            e.printStackTrace();
        }
    }
}
