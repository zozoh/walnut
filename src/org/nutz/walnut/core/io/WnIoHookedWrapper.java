package org.nutz.walnut.core.io;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.core.WnIoActionCallback;
import org.nutz.walnut.core.WnIoMappingFactory;

public class WnIoHookedWrapper extends AbstractWnIoWrapper {

    public WnIoHookedWrapper() {}

    public WnIoHookedWrapper(WnIo io) {
        super(io);

        // 准备一个写操作的回调
        this.setIo(io);
    }

    public void setIo(WnIo io) {
        this.io = io;
        // 准备一个写操作的回调
        if (io instanceof WnIoImpl2) {
            WnIoImpl2 io2 = (WnIoImpl2) io;
            // 对象写入前后的钩子
            io2.whenWrite = new WnIoActionCallback() {
                public WnObj on_before(WnObj o) {
                    WnContext wc = Wn.WC();
                    return wc.doHook("before_write", o);
                }

                public WnObj on_after(WnObj o) {
                    WnContext wc = Wn.WC();
                    return wc.doHook("write", o);
                }
            };
            // 对象删除后，从过期记录表里移除
            io2.whenDelete = new WnIoActionCallback() {
                public WnObj on_before(WnObj o) {
                    return o;
                }

                public WnObj on_after(WnObj o) {
                    tryRemoveExpiObj(o);
                    return o;
                }
            };
        }
    }

    @Override
    public WnIoMappingFactory getMappingFactory() {
        return io.getMappingFactory();
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        WnContext wc = Wn.WC();
        String mv_src = src.path();
        String mv_dst = destPath;

        src.setv("_mv_from", mv_src);
        src.setv("_mv_to", mv_dst);
        src = wc.doHook("before_move", src);

        WnObj o = io.move(src, destPath);

        o.setv("_mv_from", mv_src);
        o.setv("_mv_to", mv_dst);
        return wc.doHook("move", o);
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        WnContext wc = Wn.WC();
        String mv_from = src.path();
        String mv_to = destPath;

        src.setv("_mv_from", mv_from);
        src.setv("_mv_to", mv_to);
        src = wc.doHook("before_move", src);

        WnObj o = io.move(src, destPath, mode);

        o.setv("_mv_from", mv_from);
        o.setv("_mv_to", mv_to);
        return wc.doHook("move", src);
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        WnObj o = io.create(p, path, race);
        WnContext wc = Wn.WC();
        return wc.doHook("create", o);
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        WnObj o = io.create(p, paths, fromIndex, toIndex, race);
        WnContext wc = Wn.WC();
        return wc.doHook("create", o);
    }

    @Override
    public WnObj create(WnObj p, WnObj o) {
        WnObj o2 = io.create(p, o);
        WnContext wc = Wn.WC();
        WnObj o3 = wc.doHook("create", o2);
        // 是否有过期？
        this.tryAddExpiObj(o3);

        return o3;
    }

    @Override
    public WnObj createIfNoExists(WnObj p, String path, WnRace race) {
        WnObj o = io.createIfNoExists(p, path, race);
        WnContext wc = Wn.WC();
        return wc.doHook("create", o);
    }

    @Override
    public WnObj createIfExists(WnObj p, String path, WnRace race) {
        WnObj o = io.createIfExists(p, path, race);
        WnContext wc = Wn.WC();
        return wc.doHook("create", o);
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        WnObj o = io.createById(p, id, name, race);
        WnContext wc = Wn.WC();
        wc.doHook("create", o);
        return o;
    }

    @Override
    public void writeMeta(WnObj o, Object meta) {
        NutBean map = Wn.anyToMap(o, meta);
        WnContext wc = Wn.WC();
        io.writeMeta(o, map);
        o.put("__meta_keys", map.keySet());
        o.put("__meta", map);
        WnObj o2 = wc.doHook("meta", o);
        o.remove("__meta_keys");
        o.remove("__meta");
        o.updateBy(o2);
        if (map.containsKey("expi")) {
            this.tryAddExpiObj(o);
        }
    }

    @Override
    public void appendMeta(WnObj o, Object meta) {
        NutBean map = Wn.anyToMap(o, meta);
        WnContext wc = Wn.WC();
        io.appendMeta(o, map);
        o.put("__meta_keys", map.keySet());
        o.put("__meta", map);
        WnObj o2 = wc.doHook("meta", o);
        o.remove("__meta_keys");
        o.remove("__meta");
        o.updateBy(o2);
        if (map.containsKey("expi")) {
            this.tryAddExpiObj(o);
        }
    }

    @Override
    public void set(WnObj o, String regex) {
        WnContext wc = Wn.WC();

        Pattern p = Regex.getPattern(regex);
        NutMap meta = new NutMap();
        List<String> keys = new ArrayList<>(o.size());
        for (String key : o.keySet()) {
            if (p.matcher(key).find()) {
                keys.add(key);
                meta.put(key, o.get(key));
            }
        }

        io.set(o, regex);

        o.put("__meta_keys", keys);
        o.put("__meta", meta);
        WnObj o2 = wc.doHook("meta", o);
        o.remove("__meta_keys");
        o.remove("__meta");

        o.updateBy(o2);
        NutBean map = o.pickBy(regex);
        if (map.containsKey("expi")) {
            this.tryAddExpiObj(o);
        }
    }

    @Override
    public WnObj setBy(String id, String key, Object val, boolean returnNew) {
        WnContext wc = Wn.WC();
        WnObj o = io.setBy(id, key, val, returnNew);
        if ("expi".equals(key) && (val instanceof Long)) {
            long expi = (Long) val;
            this.tryAddExpiObj(id, expi);
        }
        return wc.doHook("meta", o);
    }

    @Override
    public WnObj setBy(WnQuery q, String key, Object val, boolean returnNew) {
        WnContext wc = Wn.WC();
        WnObj o = io.setBy(q, key, val, returnNew);
        if (null != o && "expi".equals(key) && (val instanceof Long)) {
            long expi = (Long) val;
            this.tryAddExpiObj(o.id(), expi);
        }
        return wc.doHook("meta", o);
    }

    @Override
    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        WnContext wc = Wn.WC();
        WnObj o = io.setBy(q, map, returnNew);
        if (null != o && map.containsKey("expi")) {
            this.tryAddExpiObj(o.id(), map.getLong("expi"));
        }
        return wc.doHook("meta", o);
    }

    @Override
    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        WnContext wc = Wn.WC();
        WnObj o = io.setBy(id, map, returnNew);
        if (map.containsKey("expi")) {
            this.tryAddExpiObj(id, map.getLong("expi"));
        }
        return wc.doHook("meta", o);
    }

    @Override
    public void setMount(WnObj o, String mnt) {
        WnContext wc = Wn.WC();
        String mnt_from = o.mount();
        String mnt_to = mnt;

        o.put("_mnt_from", mnt_from);
        o.put("_mnt_to", mnt_to);
        o = wc.doHook("before_mount", o);

        io.setMount(o, mnt);

        o.put("_mnt_from", mnt_from);
        o.put("_mnt_to", mnt_to);
        WnObj o2 = wc.doHook("mount", o);
        o.updateBy(o2);
    }

    @Override
    public void delete(WnObj o) {
        WnContext wc = Wn.WC();
        o = wc.doHook("before_delete", o);
        io.delete(o);
        WnObj o2 = wc.doHook("delete", o);
        o.updateBy(o2);
    }

    @Override
    public void delete(WnObj o, boolean r) {
        WnContext wc = Wn.WC();
        o = wc.doHook("before_delete", o);
        io.delete(o, r);
        WnObj o2 = wc.doHook("delete", o);
        o.updateBy(o2);
    }

}
