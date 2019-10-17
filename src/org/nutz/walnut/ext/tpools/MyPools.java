package org.nutz.walnut.ext.tpools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(depose="depose")
public class MyPools {
    
    protected boolean running = true;
    
    protected ThreadPoolExecutor top = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

    protected Map<String, ThreadPoolExecutor> ess = new ConcurrentHashMap<String, ThreadPoolExecutor>();
    
    public ThreadPoolExecutor get(String name) {
        return ess.get(name);
    }
    
    public ThreadPoolExecutor getOrCreate(String name, int size) {
        if (!running)
            return top;
        return ess.computeIfAbsent(name, (str)-> {
            int s = size;
            if (s < 2)
                s = Runtime.getRuntime().availableProcessors() * 2;
            return new ThreadPoolExecutor(2, s, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
        });
    }
    
    public void close(String name) {
        ExecutorService es = ess.remove(name);
        if (es != null)
            es.shutdown();
    }
    
    public void depose() {
        running = false;
        top.shutdown();
        for (Map.Entry<String, ThreadPoolExecutor> en : ess.entrySet()) {
            en.getValue().shutdown();
        }
    }
    
    public List<String> names() {
        return new ArrayList<>(ess.keySet());
    }
}
