package org.nutz.walnut.impl.box.docker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.nutz.lang.util.Callback;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxRuntime;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.box.WnBoxStatus;

/**
 * Docker版本的WnBox, 未完成状态
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class DockerBox implements WnBox {
    
    protected String id;
    protected WnBoxRuntime runtime;
    protected WnBoxStatus stat;
    protected WnBoxContext bc;

    protected InputStream in;
    protected OutputStream out;
    protected OutputStream err;
    
    protected Callback<WnBoxContext> on_before_free;
    
    private static final Log log = Logs.get();
    
    public DockerBox(WnBoxService boxes) {
        id = R.UU32();
        runtime = new WnBoxRuntime();
        stat = WnBoxStatus.FREE;
    }

    public String id() {
        return id;
    }

    public WnBoxStatus status() {
        return stat;
    }

    public WnBoxRuntime runtime() {
        return runtime;
    }

    public void setup(WnBoxContext bc) {
        this.bc = bc;
    }

    public void run(String cmdText) {
        // 启动一个docker容器
        ProcessBuilder pb = new ProcessBuilder("docker", "run", "-it", "--rm", "ubuntu", "/bin/bash", "-c",  cmdText);
        // 问题是, 怎么映射walnut的文件夹呢...
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

    public void onBeforeFree(Callback<WnBoxContext> handler) {}

    void free() {
        // 调用回调
        if (null != this.on_before_free)
            this.on_before_free.invoke(bc);

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
