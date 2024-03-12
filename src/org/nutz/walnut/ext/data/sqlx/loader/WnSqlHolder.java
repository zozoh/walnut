package org.nutz.walnut.ext.data.sqlx.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

/**
 * 针对某一个玉，会有一个唯一的实例，缓存这个域的所有sql模板
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSqlHolder implements SqlHolder {

    private WnIo io;
    private WnObj oDir;
    private Map<String, SqlEntry> cache;

    public WnSqlHolder() {
        cache = new HashMap<>();
    }

    public WnSqlHolder(WnIo io, WnObj oHome) {
        this();
        this.io = io;
        this.oDir = oHome;
    }

    public WnSqlHolder(WnIo io, WnAuthSession session) {
        this();
        this.io = io;
        this.oDir = Wn.checkObj(io, session, "~/.sqlx");
    }

    public WnSqlHolder(WnSystem sys) {
        this();
        this.io = sys.io;
        this.oDir = Wn.checkObj(sys, "~/.sqlx");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("CACHE %d Items", cache.size()));
        int i = 1;
        for (Map.Entry<String, SqlEntry> en : cache.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            sb.append("\n--------------------------------------------");
            sb.append(String.format("\n%d) %s => %s", i++, key, val.toString()));
        }
        sb.append("\n--------------------------------------------");
        return sb.toString();
    }

    @Override
    public WnSqlTmpl get(String key) {
        SqlEntry sqle = null;
        if (cache.containsKey(key)) {
            sqle = cache.get(key);
        }
        if (null == sqle) {
            synchronized (this) {
                if (cache.containsKey(key)) {
                    sqle = cache.get(key);
                }
                if (null == sqle) {
                    sqle = loadEntry(key);
                }
            }
        }
        if (null == sqle) {
            throw Er.create("e.sqlx.entry.NotExist", key + "@" + oDir.id());
        }
        WnSqlTmpl sqlt = WnSqlTmpl.parse(sqle);
        return sqlt;
    }

    private SqlEntry loadEntry(String key) {
        SqlEntry sqle = null;
        String[] ss = Ws.splitIgnoreBlank(key, "[.]");
        if (ss.length < 2) {
            throw Er.create("e.sqlx.invalidKeyPath", key);
        }
        // 最后一个是名称
        String name = ss[ss.length - 1];
        // 之前的是路径
        String fkey = Ws.join(ss, ".", 0, ss.length - 1);
        String path = Ws.join(ss, "/", 0, ss.length - 1) + ".sql";

        // 获取对象
        WnObj obj = io.check(oDir, path);
        String str = io.readText(obj);

        // 解析并循环计入缓存
        List<SqlEntry> list = SqlEntry.load(str);
        for (SqlEntry sql : list) {
            if (sql.hasName()) {
                String k = String.format("%s.%s", fkey, sql.getName());
                cache.put(k, sql);
                if (sql.getName().equals(name)) {
                    sqle = sql;
                }
            }
        }

        return sqle;
    }

    @Override
    public void reset() {
        cache.clear();
    }

}
