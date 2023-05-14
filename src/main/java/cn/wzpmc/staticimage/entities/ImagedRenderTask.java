package cn.wzpmc.staticimage.entities;

import cn.wzpmc.staticimage.StaticImage;
import cn.wzpmc.staticimage.renderer.ImageMapRenderer;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.awt.*;

/**
 * @author wzp
 * @version 1.0
 * @since 2023/5/14 9:46:29
 */
public class ImagedRenderTask extends RenderTask{
    private final Image image;
    public ImagedRenderTask(ItemFrame itemFrame, StaticImage plugin, World world, String imageName, int[] xywh, Image image) {
        super(itemFrame, plugin, world, imageName, xywh);
        this.image = image;
    }

    @Override
    public void run() {
        ItemStack item = super.frame.getItem();
        MapMeta itemMeta = (MapMeta) item.getItemMeta();
        MapView mapView = super.server.createMap(super.world);
        mapView.getRenderers().forEach(mapView::removeRenderer);
        mapView.addRenderer(new ImageMapRenderer(image));
        itemMeta.setMapView(mapView);
        item.setItemMeta(itemMeta);
        frame.setItem(item);
    }
}
