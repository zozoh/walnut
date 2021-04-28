package org.nutz.walnut.ext.sys.quota;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.WnConfig;
import org.nutz.web.Webs.Err;

import com.mongodb.AggregationOptions;
import com.mongodb.Cursor;
import com.mongodb.DBObject;

/**
 * 统一管理系统的内的配额,例如磁盘配额, 流量配额
 * @author wendal
 *
 */
@IocBean(create="init", depose="depose", name="quota")
public class QuotaService {
    
    private static final Log log = Logs.get();

    @Inject("java:$mongoDB.getCollection('obj')")
    protected ZMoCo co;

    @Inject
    protected WnIo io;
    
    @Inject
    protected WnConfig conf;
    
    protected ExecutorService es;
    
    protected WnObj quotaRoot;
    
    // 查询配额和消耗量是很耗时的,需要缓存起来
    protected boolean running = true;
    protected Map<String, Long> qu = new HashMap<>();
    protected Map<String, AtomicLong> ug = new HashMap<>();
    
    public boolean checkQuota(String type, String username, boolean just_throw) {
        if (username == null || "root".equals(username))
            return true;
        // 看看有无限额限制
        Long quota = qu.get(username + ":" + type);
        if (quota == null || quota < 0)
            return true;
        // 有无已用空间统计数据
        AtomicLong used = ug.get(username + ":" + type);
        if (used == null)
            return true;
        if (used.get() >= quota) {
            if (just_throw)
                throw Err.create("e.io.over_quota");
            return false;
        }
        return true;
    }
    
    public long getQuota(String type, String username, boolean realtime) {
        if (username == null || "root".equals(username))
            return -1L;
        if (realtime) {
            WnObj wobj = io.fetch(quotaRoot, username);
            if (wobj == null)
                return -1L;
            return wobj.getLong("quota_" + type, -1L);
        } else {
            return qu.getOrDefault(username + ":" + type, -1L);
        }
    }
    
    public long getUsage(String type, String username, boolean realtime) {
        if (username == null || "root".equals(username))
            return -1L;
        if (realtime && "disk".equals(type))
            return getUserDiskUsageRealtime(username);
        AtomicLong usage = ug.get(username + ":" + type);
        if (usage == null)
            return -1L;
        return usage.get();
    }
    
    public void setQuota(String type, String username, long quota) {
        WnObj wobj = io.createIfNoExists(quotaRoot, username, WnRace.FILE);
        io.setBy(wobj.id(), new NutMap("quota_" + type, quota), false);
    }

    /**
     * 获取用户的空间占用情况,比较重的操作,不宜经常调用
     * @param userName 用户名
     * @return 已用空间,仅统计/home/$user下的空间
     */
    protected long getUserDiskUsageRealtime(String userName) {
        if ("root".equals(userName))
            return 0;
        ZMoDoc match = ZMoDoc.NEW("{$match:{d0:'home', 'd1':'" + userName + "'}}");
        ZMoDoc group = ZMoDoc.NEW("{$group:{_id:'$d1', 'disk_usage':{'$sum':'$len'}}}");
        Cursor cursor = co.aggregate(Arrays.asList(match, group), AggregationOptions.builder().build());
        try {
            if (cursor.hasNext()) {
                DBObject dbo = cursor.next();
                return ((Number) dbo.get("disk_usage")).longValue();
            }
        }
        finally {
            cursor.close();
        }
        return 0;
    }
    
    /**
     * 更新全部用户的空间占用率
     */
    public void updateQuotaAndFlushUsage(boolean doInit) {
        
        for (WnObj wobj : io.getChildren(quotaRoot, null)) {
            for (Entry<String, Object> en : wobj.entrySet()) {
                String key = en.getKey();
                if (key.startsWith("quota_")) {
                    Long qouta = (Long)en.getValue();
                    if (qouta == null) {
                        qouta = -1L;
                    }
                    qu.put(wobj.name() + ":" + key.substring("quota_".length()), qouta);

                    // 仅检查有配额限制的用户
                    if ("quota_disk".equals(key) && qouta > -1) {
                        ug.put(wobj.name() + ":disk", new AtomicLong(this.getUserDiskUsageRealtime(wobj.name())));
                    }
                }
                else if (doInit && key.startsWith("usage_") && !"quota_disk".equals(key)) {
                    ug.put(wobj.name() + ":" + key.substring("usage_".length()), new AtomicLong((Long)en.getValue()));
                }
            }
        }
        // 把使用量持久化
        if (!doInit) {
            for (Entry<String, AtomicLong> en : ug.entrySet()) {
                String[] tmp = en.getKey().split(":");
                String username = tmp[0];
                String type = tmp[1];
                if (type.endsWith(":disk")) // 磁盘空间不需要持久化
                    continue;
                io.setBy(Wn.Q.pid(quotaRoot.id()).setv("nm", username), new NutMap("usage_" + type, en.getValue().get()), false);
            }
        }
    }

    
    public void incrUsage(String username, String type, long len) {
        String key = username + ":" + type;
        AtomicLong atom = ug.get(key);
        if (atom == null) {
            atom = new AtomicLong();
            ug.put(key, atom);
        }
        atom.addAndGet(len);
    }
    
    public void init() {
        quotaRoot = io.createIfNoExists(null, "/sys/quota", WnRace.DIR);
        updateQuotaAndFlushUsage(true);
        es = Executors.newCachedThreadPool();
        es.submit(()-> {
            int count = 0;
            int interval = conf.getInt("quota-update-interval", 300);
            while (running) {
                Lang.quiteSleep(1000);
                try {
                    if (count % interval == 0) {
                        updateQuotaAndFlushUsage(false);
                    }
                    count ++;
                }
                catch (Throwable e) {
                    log.info("update user quota/usage FAIL!!", e);
                }
            }
        });
    }
    
    public void depose() {
        running = false;
        if (es != null)
            es.shutdown();
    }
}
