package org.nutz.walnut.ext.quota;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Lang;
import org.nutz.lang.LoopException;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.web.Webs.Err;

import com.mongodb.AggregationOptions;
import com.mongodb.Cursor;
import com.mongodb.DBObject;

/**
 * 统一管理系统的内的配额,例如磁盘配额, 流量配额
 * @author wendal
 *
 */
@IocBean(create="init", depose="depose")
public class QuotaService {

    @Inject("java:$mongoDB.getCollection('obj')")
    protected ZMoCo co;

    @Inject
    protected WnUsrService usrs;

    @Inject
    protected WnIo io;
    
    protected ExecutorService es;
    
    // 查询配额和消耗量是很耗时的,需要缓存起来
    protected Map<String, Long> diskQuota = new HashMap<>();
    protected Map<String, Long> diskUsage = new HashMap<>();
    protected Map<String, Long> networkUsage = new HashMap<>();
    protected Map<String, Long> networkQuota = new HashMap<>();
    protected boolean running = true;
    
    public void checkQuota(String type, String username) {
        if (username == null || "root".equals(username))
            return;
        // 看看这个用户有无限额
        Long quota = diskQuota.get(username);
        if (quota == null)
            return;
        // 有无已用空间统计数据
        Long used = diskUsage.get(username);
        if (used == null)
            return;
        if (used >= quota)
            throw Err.create("e.io.over_quota");
    }
    
    public Long getQuota(String type, String username, boolean realtime) {
        if (username == null || "root".equals(username))
            return -1L;
        if (realtime) {
            WnUsr usr = usrs.check(username);
            return usr.getLong("quota_" + type, -1L);
        } else {
            switch (type) {
            case "disk":
                return diskQuota.get(username);
            case "network":
                return networkQuota.get(username);
            default:
                break;
            }
        }
        return -1L;
    }
    
    public Long getUsage(String type, String username, boolean realtime) {
        if (username == null || "root".equals(username))
            return -1L;
        switch (type) {
        case "disk":
            if (realtime) {
                return getUserDiskUsageRealtime(username);
            } else {
                return diskUsage.get(username);
            }
        case "network":
            return networkUsage.get(username);
        default:
            break;
        }
        return -1L;
    }
    
    public void setQuota(String type, String username, long quota) {
        if (username == null || "root".equals(username))
            return;
        WnUsr usr = usrs.check(username);
        usrs.set(usr, "quota_" + username, quota);
    }

    /**
     * 获取用户的空间占用情况,比较重的操作,不宜经常调用
     * @param userName 用户名
     * @return 已用空间,仅统计/home/$user下的空间
     */
    protected long getUserDiskUsageRealtime(String userName) {
        if ("root".equals(userName))
            return 0;
        WnUsr usr = usrs.check(userName);
        ZMoDoc match = ZMoDoc.NEW("{$match:{d0:'home', 'd1':'" + usr.name() + "'}}");
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
    public void updateUserQuotaDiskUsage() {
        List<String> usernames = new ArrayList<>();
        usrs.each(null, new Each<WnUsr>() {
            public void invoke(int index, WnUsr ele, int length)
                    throws ExitLoop, ContinueLoop, LoopException {
                usernames.add(ele.name());
            }
        });
        for (String username : usernames) {
            diskUsage.put(username, this.getUsage("disk", username, true));
            diskQuota.put(username, this.getQuota("disk", username, true));
        }
    }

    
    public void incrNetworkUsage(String hostname, long income, long outgo) {
        
    }
    
    public void init() {
        es = Executors.newCachedThreadPool();
        es.submit(()-> {
            int count = 0;
            while (running) {
                Lang.quiteSleep(1000);
                if (count % 60 == 0) {
                    updateUserQuotaDiskUsage();
                }
                count ++;
            }
        });
    }
    
    public void depose() {
        running = false;
        if (es != null)
            es.shutdown();
    }
}
