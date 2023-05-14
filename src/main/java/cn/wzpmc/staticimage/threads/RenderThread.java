package cn.wzpmc.staticimage.threads;

import cn.wzpmc.staticimage.entities.RenderTask;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wzp
 * @version 1.0
 * @since 2023/5/14 9:47:33
 */
public class RenderThread extends BukkitRunnable {
    private final List<RenderTask> renderTask = new ArrayList<>();
    public void addTask(RenderTask task){
        this.renderTask.add(task);
    }

    @Override
    public void run() {
        while (!renderTask.isEmpty()) {
            RenderTask remove = this.renderTask.remove(0);
            remove.run();
        }
    }
}
