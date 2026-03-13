package com.site0.walnut.ext.data.sqlx.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.sqlx.tmpl.WnSqlTmpl;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

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
        cache = new WeakHashMap<>();
    }

    public WnSqlHolder(WnIo io, WnObj oHome) {
        this();
        this.io = io;
        this.oDir = oHome;
    }

    public WnSqlHolder(WnIo io, WnSession session) {
        this();
        this.io = io;
        this.oDir = Wn.checkObj(io, session, "~/.sqlx");
    }

    public WnSqlHolder(WnSystem sys) {
        this();
        this.io = sys.io;
        this.oDir = Wn.checkObj(sys, "~/.sqlx");
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean equals(Object input) {
        if (this == input) {
            return true;
        }
        if (null == input) {
            return false;
        }
        if (input instanceof WnSqlHolder) {
            WnSqlHolder ta = (WnSqlHolder) input;
            return ta.oDir.equals(this.oDir);
        }
        return false;
    }

    public String toString() {
        return SqlEntry.dumpToStr(cache);
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

    @Override
    synchronized public Map<String, SqlEntry> find(String keywords) {
        WnMatch am = AutoMatch.parse(keywords, false);
        List<String> found_keys = new ArrayList<>(cache.size());
        Set<String> kset = cache.keySet();
        for (String key : kset) {
            if (am.match(key)) {
                found_keys.add(key);
            }
        }

        Map<String, SqlEntry> map = new HashMap<>();
        for (String key : found_keys) {
            SqlEntry sqle = cache.get(key);
            map.put(key, sqle.clone());
        }
        return map;
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
    public void clear() {
        synchronized (this) {
            cache.clear();
        }
    }

}
