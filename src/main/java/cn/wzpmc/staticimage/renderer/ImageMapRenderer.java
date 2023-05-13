package cn.wzpmc.staticimage.renderer;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author wzp
 * @version 1.0
 * @since 2023/5/13 19:56:26
 */
public class ImageMapRenderer extends MapRenderer {
    //250ms 4fps
    private final Image image;
    public ImageMapRenderer(Image image){
        this.image = image;
    }
    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        canvas.drawImage(0,0, this.image);
    }
}
