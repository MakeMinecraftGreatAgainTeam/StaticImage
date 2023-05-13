package cn.wzpmc.staticimage.events;

import cn.wzpmc.staticimage.StaticImage;
import cn.wzpmc.staticimage.renderer.ImageMapRenderer;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author wzp
 * @version 1.0
 * @since 2023/5/13 21:17:37
 */
public class LoadChunk implements Listener {
    private final Logger logger;
    private final Server server;
    private final StaticImage plugin;
    public LoadChunk(){
        this.plugin = JavaPlugin.getPlugin(StaticImage.class);
        logger = this.plugin.getSLF4JLogger();
        server = this.plugin.getServer();
    }
    @EventHandler
    public void onLoadChunk(PlayerChunkLoadEvent event){
        new LoadTask(event).runTaskAsynchronously(plugin);
    }
    private class LoadTask extends BukkitRunnable {
        private final PlayerChunkLoadEvent event;
        private LoadTask(PlayerChunkLoadEvent event){
            this.event = event;
        }

        @Override
        public void run() {
            Chunk chunk = event.getChunk();
            Stream<Boolean> booleanStream = Arrays.stream(chunk.getEntities())
                    .filter(entity -> entity.getType().equals(EntityType.ITEM_FRAME))
                    .map(entity -> {
                        ItemFrame frame = (ItemFrame) entity;
                        ItemStack item = frame.getItem();
                        if (item.getType().equals(Material.FILLED_MAP)) {
                            MapMeta itemMeta = (MapMeta) item.getItemMeta();
                            MapView view = itemMeta.getMapView();
                            if (view != null){
                                if (view.getRenderers().stream().anyMatch(mapRenderer -> mapRenderer instanceof ImageMapRenderer)) {
                                    return false;
                                }
                            }
                            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
                            if (persistentDataContainer.has(StaticImage.getTagKey(), PersistentDataType.STRING)) {
                                String imageName = persistentDataContainer.get(StaticImage.getTagKey(), PersistentDataType.STRING);
                                int[] xywh = persistentDataContainer.get(StaticImage.getXywhKey(), PersistentDataType.INTEGER_ARRAY);
                                assert xywh != null;
                                File file = new File(LoadChunk.this.plugin.getDataFolder(), imageName + ".png");
                                if (!file.exists()){
                                    file = new File(LoadChunk.this.plugin.getDataFolder(), imageName + ".jpg");
                                }
                                if (!file.exists()){
                                    logger.error("无法读取图片数据，物品：{}，图片：{}", item, imageName);
                                    return true;
                                }
                                try {
                                    FileInputStream fileInputStream = new FileInputStream(file);
                                    BufferedImage image = ImageIO.read(fileInputStream);
                                    fileInputStream.close();
                                    MapView mapView = server.createMap(chunk.getWorld());
                                    mapView.getRenderers().forEach(mapView::removeRenderer);
                                    mapView.addRenderer(new ImageMapRenderer(image.getSubimage(xywh[0], xywh[1], xywh[2], xywh[3])));
                                    itemMeta.setMapView(mapView);
                                    item.setItemMeta(itemMeta);
                                    frame.setItem(item);
                                } catch (IOException e) {
                                    logger.error("无法读取图片数据，物品：{}，图片：{}", item, imageName);
                                    e.printStackTrace();
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
            if (booleanStream.anyMatch(Boolean::booleanValue)) {
                logger.info("区块物品数据读取出现错误，区块X：{}，Z：{}", chunk.getX(), chunk.getZ());
            }
        }
    }
}
