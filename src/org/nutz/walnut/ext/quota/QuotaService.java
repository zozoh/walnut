package org.nutz.walnut.ext.quota;

import java.util.Arrays;
import java.util.List;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;

import com.mongodb.AggregationOptions;
import com.mongodb.Cursor;
import com.mongodb.DBObject;

@IocBean
public class QuotaService {

    @Inject("java:$mongoDB.getCollection('obj')")
    protected ZMoCo co;

    @Inject
    protected WnUsrService usrs;

    @Inject
    protected WnIo io;

    // ==============================================
    // 磁盘空间相关

    public void checkDiskQuota(WnObj wobj) {

    }

    public long getUserDiskQuota(String userName) {
        if ("root".equals(userName))
            return -1;
        return usrs.check(userName).getLong("quota_disk", -1);
    }

    public void setUserDiskQuota(String userName, long quote) {
        if ("root".equals(userName))
            return;
        WnUsr usr = usrs.check(userName);
        usr.setv("quota_disk", quote);
        usrs.set(usr, "quote_disk", quote);
    }

    public long getUserDiskUsage(String userName) {
        if ("root".equals(userName))
            return 0;
        WnUsr usr = usrs.check(userName);
        ZMoDoc match = ZMoDoc.NEW("{$match:{d0:'home', 'd1':'" + usr.name() + "'}}");
        ZMoDoc group = ZMoDoc.NEW("{$group:{_id:'$d1', 'disk_usage':{'$sum':'$len'}}}");
        Cursor cursor = co.aggregate(Arrays.asList(match, group),
                                     AggregationOptions.builder().build());
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

    // --------------------------------------------------------------
    // 网络流量相关
    public void checkNetworkQuota(String hostname) {

    }

    public long getHostnameNetworkQuota(String hostname) {
        if ("root".equals(hostname))
            return -1;
        WnObj web = getDomainByHostname(hostname);
        if (web == null)
            return -1;
        return web.getLong("quota_network", -1);
    }

    public void setHostnameNetworkQuota(String hostname, long quote) {
        WnObj web = getDomainByHostname(hostname);
        if (web == null)
            return;
        web.setv("quota_network", quote);
        io.set(web, "quota_network");
    }

    public long getHostnameNetworkUsage(String hostname) {
        WnObj web = getDomainByHostname(hostname);
        if (web == null)
            return 0;
        return 0;
    }

    public WnObj getDomainByHostname(String hostname) {
        List<WnObj> webs = io.query(new WnQuery().setv("d0", "domain").setv("dmn_host", hostname));
        if (webs.isEmpty())
            return null;
        return webs.get(0);
    }
}
