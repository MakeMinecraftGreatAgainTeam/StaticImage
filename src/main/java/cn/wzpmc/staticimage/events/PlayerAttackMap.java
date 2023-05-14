package cn.wzpmc.staticimage.events;

import cn.wzpmc.staticimage.StaticImage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;

/**
 * @author wzp
 * @version 1.0
 * @since 2023/5/14 0:12:22
 */
public class PlayerAttackMap implements Listener {
    @EventHandler
    public void onPlayerAttackMap(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        if (entity.getType().equals(EntityType.ITEM_FRAME)) {
            ItemFrame frame = (ItemFrame) entity;
            PersistentDataContainer persistentDataContainer = frame.getItem().getItemMeta().getPersistentDataContainer();
            if (persistentDataContainer.has(StaticImage.getTagKey())) {
                Entity damager = event.getDamager();
                if (damager instanceof Player player) {
                    if (player.hasPermission("staticimage.bypass")){
                        return;
                    }
                    event.setCancelled(true);
                    damager.sendMessage(Component.text("请不要尝试破坏地图画！").color(TextColor.color(170,0,0)));
                }
            }
        }
    }
    @EventHandler
    public void onPlayerUseOnMap(PlayerInteractEntityEvent event){
        Player player = event.getPlayer();
        Entity entity = player.getTargetEntity(5);
        if (entity == null){
            return;
        }
        if (entity.getType().equals(EntityType.ITEM_FRAME)) {
            ItemFrame frame = (ItemFrame) entity;
            PersistentDataContainer persistentDataContainer = frame.getItem().getItemMeta().getPersistentDataContainer();
            if (persistentDataContainer.has(StaticImage.getTagKey())) {
                if (player.hasPermission("staticimage.bypass")){
                    return;
                }
                event.setCancelled(true);
                player.sendMessage(Component.text("请不要尝试修改地图画！").color(TextColor.color(170,0,0)));
            }
        }
    }
}
