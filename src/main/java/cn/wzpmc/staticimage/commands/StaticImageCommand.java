package cn.wzpmc.staticimage.commands;

import cn.wzpmc.staticimage.StaticImage;
import cn.wzpmc.staticimage.renderer.ImageMapRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author wzp
 * @version 1.0
 * @since 2023/5/13 19:15:45
 */
public class StaticImageCommand implements CommandExecutor, TabCompleter {
    private final StaticImage plugin;
    private final Server server;

    public StaticImageCommand(){
        this.plugin = JavaPlugin.getPlugin(StaticImage.class);
        this.server = plugin.getServer();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try{
            if (sender instanceof Player player){
                if (args[0].equals("get")) {
                    String imageName = args[1];
                    File file = new File(plugin.getDataFolder(), imageName + ".png");
                    if (!file.exists()){
                        file = new File(plugin.getDataFolder(), imageName + ".jpg");
                    }
                    if (!file.exists()){
                        sender.sendMessage(Component.text("图片不存在").style(Style.style(TextColor.color(170,0,0))));
                        return true;
                    }
                    FileInputStream fileInputStream = new FileInputStream(file);
                    BufferedImage image = ImageIO.read(fileInputStream);
                    fileInputStream.close();
                    int height = image.getHeight();
                    int width = image.getWidth();
                    Location location = player.getLocation().clone().add(0,2,0);
                    PlayerInventory playerInventory = player.getInventory();
                    Block block = location.getBlock();
                    block.setType(Material.SHULKER_BOX);
                    ShulkerBox box = (ShulkerBox) block.getState();
                    Inventory inventory = box.getInventory();
                    int lines = 0;
                    int rows = 0;
                    int i = 1;
                    for (int y = 0; y < height; y += 128) {
                        rows = 0;
                        for (int x = 0; x < width; x += 128) {
                            int w = 128;
                            int h = 128;
                            if (x + w > width){
                                w = width - x - 1;
                            }
                            if (y + h > height){
                                h = height - y - 1;
                            }
                            BufferedImage subImg = image.getSubimage(x, y, w, h);
                            ItemStack map = new ItemStack(Material.FILLED_MAP);
                            MapMeta mapMeta = (MapMeta) map.getItemMeta();
                            MapView mapView = server.createMap(player.getWorld());
                            mapView.getRenderers().forEach(mapView::removeRenderer);
                            mapView.addRenderer(new ImageMapRenderer(subImg));
                            mapMeta.setMapView(mapView);
                            PersistentDataContainer persistentDataContainer = mapMeta.getPersistentDataContainer();
                            persistentDataContainer.set(StaticImage.getTagKey(), PersistentDataType.STRING, imageName);
                            persistentDataContainer.set(StaticImage.getXywhKey(), PersistentDataType.INTEGER_ARRAY, new int[]{x, y, w, h});
                            mapMeta.displayName(Component.text(rows).appendSpace().append(Component.text(lines)));
                            map.setItemMeta(mapMeta);
                            int firstEmpty = inventory.firstEmpty();
                            if (firstEmpty == -1){
                                ItemStack boxItem = (ItemStack) block.getDrops().toArray()[0];
                                ItemMeta itemMeta = boxItem.getItemMeta();
                                itemMeta.displayName(Component.text(i));
                                boxItem.setItemMeta(itemMeta);
                                playerInventory.addItem(boxItem);
                                inventory.clear();
                                i++;
                                firstEmpty = 0;
                            }
                            inventory.setItem(firstEmpty, map);
                            rows++;
                        }
                        lines++;
                    }
                    ItemStack boxItem = (ItemStack) block.getDrops().toArray()[0];
                    ItemMeta itemMeta = boxItem.getItemMeta();
                    itemMeta.displayName(Component.text(i));
                    boxItem.setItemMeta(itemMeta);
                    playerInventory.addItem(boxItem);
                    inventory.clear();
                    location.getBlock().setType(Material.AIR);
                    sender.sendMessage(Component.text("共" + lines + "行" + rows + "列"));
                }else {
                    sender.sendMessage(Component.text("StaticImage").style(Style.style(TextColor.color(255, 170, 0))).append(Component.text("帮助：").style(Style.style(TextColor.color(170,170,170)))).appendNewline()
                            .append(Component.text("/staticimage get [name]     获取地图画")).appendNewline()
                            .append(Component.text("/staticimage help           获取帮助")));
                }
            }
        }catch (ArrayIndexOutOfBoundsException e){
            sender.sendMessage(Component.text("StaticImage").style(Style.style(TextColor.color(255, 170, 0))).append(Component.text("帮助：").style(Style.style(TextColor.color(170,170,170)))).appendNewline()
                    .append(Component.text("/staticimage get [name]     获取地图画")).appendNewline()
                    .append(Component.text("/staticimage help           获取帮助")));
        } catch (IOException e) {
            sender.sendMessage(Component.text("无法读取文件，报错已发往后台").style(Style.style(TextColor.color(170,0,0))));
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length <= 1){
            result.add("help");
            result.add("get");
        } else if (args.length == 2){
            if (args[0].equals("get")){
                result.addAll(List.of(Objects.requireNonNull(plugin.getDataFolder().list())));
            }
        }
        return result.stream().filter(e -> e.contains(args[args.length - 1])).map(e -> e.replace(".png","").replace(".jpg", "")).toList();
    }
}
