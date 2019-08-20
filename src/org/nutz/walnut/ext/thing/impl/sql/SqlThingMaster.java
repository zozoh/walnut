package org.nutz.walnut.ext.thing.impl.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.io.WnBean;

import com.alibaba.druid.pool.DruidDataSource;

@IocBean(depose = "depose")
public class SqlThingMaster {

    protected Map<String, Dao> daos = new ConcurrentHashMap<>();
    protected Map<String, DruidDataSource> dataSources = new ConcurrentHashMap<>();

    @Inject
    protected WnIo io;

    private static SqlThingMaster _me;

    public SqlThingMaster() {
        _me = this;
    }

    public SqlThingContext getSqlThingContext(WnObj oTs) {
        SqlThingContext ctx = new SqlThingContext();
        ctx.dao = getDao(oTs.creator(), oTs.getString("thing-sql-ds"));
        ctx.table = oTs.getString("thing-sql-table");
        return ctx;
    }

    public Dao getDao(String user, String dsName) {
        String key = user + ":" + dsName;
        return daos.computeIfAbsent(key, (_key) -> {
            WnObj wobj = io.check(null, "/home/" + user + "/.sqlthing/" + dsName + "/conf");
            NutMap map = io.readJson(wobj, NutMap.class);
            if (!map.containsKey("maxWait")) {
                map.put("maxWait", 2000);
            }
            if (!map.containsKey("maxActive")) {
                map.put("maxActive", 50);
            }
            DruidDataSource ds = Lang.map2Object(map, DruidDataSource.class);
            dataSources.put(key, ds);
            return new NutDao(ds);
        });
    }

    public void depose() {
        for (Entry<String, DruidDataSource> en : dataSources.entrySet()) {
            en.getValue().close();
        }
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public static SqlThingMaster me() {
        return _me;
    }

    public static List<WnObj> asWnObj(WnObj oTs, WnObj parent, List<Map<String, Object>> objs) {
        ArrayList<WnObj> list = new ArrayList<>(objs.size());
        for (Map<String, Object> map : objs) {
            list.add(asWnObj(oTs, parent, map));
        }
        return list;
    }

    public static WnObj asWnObj(WnObj oTs, WnObj parent, Map<String, Object> map) {
        WnObj wobj = new WnBean();
        wobj.putAll(map);
        wobj.race(WnRace.FILE);
        wobj.d0(oTs.d0());
        wobj.d1(oTs.d1());
        wobj.creator(oTs.creator());
        wobj.mender(oTs.mender());
        wobj.group(oTs.group());
        wobj.mode(oTs.mode());
        wobj.setParent(parent);
        return wobj;
    }
}
