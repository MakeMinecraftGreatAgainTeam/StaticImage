package cn.wzpmc.staticimage.events;

import cn.wzpmc.staticimage.entities.RenderTask;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wzp
 * @version 1.0
 * @since 2023/5/14 0:29:33
 */
public class RenderTaskRunner extends BukkitRunnable {
    private static final List<RenderTask> tasks = new ArrayList<>();
    @Override
    public void run() {
        long time = new Date().getTime();
        while (!tasks.isEmpty()){
            RenderTask remove = tasks.remove(0);
            remove.run();
            if (new Date().getTime() - time >= 50){
                break;
            }
        }
    }
    public void addTask(RenderTask renderTask){
        tasks.add(renderTask);
    }
}
