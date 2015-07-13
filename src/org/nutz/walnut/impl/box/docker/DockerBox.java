package org.nutz.walnut.impl.box.docker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.nutz.lang.util.Callback;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.box.WnBoxStatus;

/**
 * Docker版本的WnBox, 未完成状态
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class DockerBox implements WnBox {
    
    protected String id;
    protected WnBoxStatus stat;
    protected WnBoxContext bc;
    protected String fuseRoot;

    protected InputStream in;
    protected OutputStream out;
    protected OutputStream err;
    
    protected Callback<WnBoxContext> on_before_free;
    
    private static final Log log = Logs.get();
    
    public DockerBox(WnBoxService boxes, String fuseRoot) {
        id = R.UU32();
        stat = WnBoxStatus.FREE;
        this.fuseRoot = fuseRoot;
    }

    public String id() {
        return id;
    }

    public WnBoxStatus status() {
        return stat;
    }

    public void setup(WnBoxContext bc) {
        this.bc = bc;
    }

    public void run(String cmdText) {
        // 准备启动参数
        List<String> cmds = new ArrayList<>();
        cmds.addAll(Arrays.asList("docker", "run", "-it", "--rm")); //基本参数
        // 将walnut的根目录映射的到一个固定路径, 外部路径带上id和用户名,就能在fuse中分辨Box和用户了
        cmds.addAll(Arrays.asList("-v", String.format("/%s/.dockerbox/%s/%s:/walnut_root", fuseRoot, bc.me.name(), id)));
        cmds.add("walnut/dockerbox"); // 定制好的镜像
        // walnut_docker_box_run的工作: 在执行环境中,构建一个新的根文件夹系统(/bin,$HOME),然后chroot过去,最后用bash执行cmdText
        cmds.addAll(Arrays.asList("/walnut_docker_box_run", "-HOME="+bc.session.envs().getString("HOME"), "-CMD="+cmdText));
        // 启动一个docker容器
        ProcessBuilder pb = new ProcessBuilder(cmds);
        ExecutorService es = Executors.newFixedThreadPool(3);
        try {
            stat = WnBoxStatus.RUNNING;
            Process p = pb.start();
            es.submit(new PipeIoThread(cmdText, in, p.getOutputStream()));
            es.submit(new PipeIoThread(cmdText, p.getInputStream(), out));
            es.submit(new PipeIoThread(cmdText, p.getErrorStream(), err));
            p.waitFor();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            es.shutdownNow();
            stat = WnBoxStatus.IDLE;
        }
    }

    public void setStdout(OutputStream ops) {
        this.out = ops;
    }

    public void setStderr(OutputStream ops) {
        this.err = ops;
    }

    public void setStdin(InputStream ins) {
        this.in = ins;
    }

    public void onBeforeFree(Callback<WnBoxContext> handler) {
        this.on_before_free = handler;
    }

    void free() {
        // 调用回调
        if (null != this.on_before_free) {
            this.on_before_free.invoke(bc);
            this.on_before_free = null;
        }

        // 释放主运行器? what?
        //runner.__free();

        // 释放其他资源
        if (log.isDebugEnabled())
            log.debug("box: release resources");
        Streams.safeClose(out);
        Streams.safeClose(in);
        Streams.safeClose(err);
        stat = WnBoxStatus.FREE;
    }
}

class PipeIoThread implements Runnable {
    protected String cmdText;
    protected InputStream in;
    protected OutputStream out;
    public PipeIoThread(String cmdText, InputStream in, OutputStream out) {
        super();
        this.cmdText = cmdText;
        this.in = in;
        this.out = out;
    }
    
    public void run() {
        if (in == null)
            return;
        while (true) {
            try {
                int b = in.read();
                if (b == -1)
                    break;
                if (out != null)
                    out.write(b);
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        Streams.safeClose(in);
        Streams.safeClose(out);
    }
}
