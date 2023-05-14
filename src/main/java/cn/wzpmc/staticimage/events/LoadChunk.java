package cn.wzpmc.staticimage.events;

import cn.wzpmc.staticimage.StaticImage;
import cn.wzpmc.staticimage.entities.RenderTask;
import cn.wzpmc.staticimage.renderer.ImageMapRenderer;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author wzp
 * @version 1.0
 * @since 2023/5/13 21:17:37
 */
public class LoadChunk implements Listener {
    private final Logger logger;
    private final StaticImage plugin;
    public LoadChunk(){
        this.plugin = JavaPlugin.getPlugin(StaticImage.class);
        logger = this.plugin.getSLF4JLogger();
    }
    @EventHandler
    public void onLoadChunk(PlayerChunkLoadEvent event){
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
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
                        PersistentDataContainer persistentDataContainer = item.getItemMeta().getPersistentDataContainer();
                        if (persistentDataContainer.has(StaticImage.getTagKey())) {
                            String imageName = persistentDataContainer.get(StaticImage.getTagKey(), PersistentDataType.STRING);
                            int[] xywh = persistentDataContainer.get(StaticImage.getXywhKey(), PersistentDataType.INTEGER_ARRAY);
                            this.plugin.getRenderThread().addTask(new RenderTask(frame, this.plugin, world, imageName, xywh));
                        }
                        return false;
                    }
                    return false;
                });
        if (booleanStream.anyMatch(Boolean::booleanValue)) {
            logger.info("区块物品数据读取出现错误，区块X：{}，Z：{}", chunk.getX(), chunk.getZ());
        }
    }
}
