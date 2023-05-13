package cn.wzpmc.staticimage.entities;

import cn.wzpmc.staticimage.StaticImage;
import cn.wzpmc.staticimage.renderer.ImageMapRenderer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
public class RenderTask implements Runnable{
    private final ItemFrame frame;
    private final StaticImage plugin;
    private final Logger logger;
    private final Server server;
    private final World world;
    public RenderTask(ItemFrame itemFrame, StaticImage plugin, World world){
        this.frame = itemFrame;
        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
        this.server = plugin.getServer();
        this.world = world;
    }

    @Override
    public void run() {
        ItemStack item = this.frame.getItem();
        MapMeta itemMeta = (MapMeta) item.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        if (persistentDataContainer.has(StaticImage.getTagKey(), PersistentDataType.STRING)) {
            String imageName = persistentDataContainer.get(StaticImage.getTagKey(), PersistentDataType.STRING);
            int[] xywh = persistentDataContainer.get(StaticImage.getXywhKey(), PersistentDataType.INTEGER_ARRAY);
            assert xywh != null;
            File file = new File(this.plugin.getDataFolder(), imageName + ".png");
            if (!file.exists()){
                file = new File(this.plugin.getDataFolder(), imageName + ".jpg");
            }
            if (!file.exists()){
                logger.error("无法读取图片数据，物品：{}，图片：{}", item, imageName);
            }
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedImage image = ImageIO.read(fileInputStream);
                fileInputStream.close();
                MapView mapView = server.createMap(this.world);
                mapView.getRenderers().forEach(mapView::removeRenderer);
                mapView.addRenderer(new ImageMapRenderer(image.getSubimage(xywh[0], xywh[1], xywh[2], xywh[3])));
                itemMeta.setMapView(mapView);
                item.setItemMeta(itemMeta);
                frame.setItem(item);
            } catch (IOException e) {
                logger.error("无法读取图片数据，物品：{}，图片：{}", item, imageName);
                e.printStackTrace();
            }
        }
    }
}
