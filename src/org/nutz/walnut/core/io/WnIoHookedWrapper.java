package org.nutz.walnut.core.io;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.core.WnIoActionCallback;

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
        }
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
        WnContext wc = Wn.WC();
        io.writeMeta(o, meta);
        WnObj o2 = wc.doHook("meta", o);
        o.update2(o2);
    }

    @Override
    public void appendMeta(WnObj o, Object meta) {
        WnContext wc = Wn.WC();
        io.appendMeta(o, meta);
        WnObj o2 = wc.doHook("meta", o);
        o.update2(o2);
    }

    @Override
    public void set(WnObj o, String regex) {
        WnContext wc = Wn.WC();
        io.set(o, regex);
        WnObj o2 = wc.doHook("meta", o);
        o.update2(o2);
    }

    @Override
    public WnObj setBy(String id, String key, Object val, boolean returnNew) {
        WnContext wc = Wn.WC();
        WnObj o = io.setBy(id, key, val, returnNew);
        return wc.doHook("meta", o);
    }

    @Override
    public WnObj setBy(WnQuery q, String key, Object val, boolean returnNew) {
        WnContext wc = Wn.WC();
        WnObj o = io.setBy(q, key, val, returnNew);
        return wc.doHook("meta", o);
    }

    @Override
    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        WnContext wc = Wn.WC();
        WnObj o = io.setBy(q, map, returnNew);
        return wc.doHook("meta", o);
    }

    @Override
    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        WnContext wc = Wn.WC();
        WnObj o = io.setBy(id, map, returnNew);
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
        o.update2(o2);
    }

    @Override
    public void delete(WnObj o) {
        WnContext wc = Wn.WC();
        o = wc.doHook("before_delete", o);
        io.delete(o);
        WnObj o2 = wc.doHook("delete", o);
        o.update2(o2);
    }

    @Override
    public void delete(WnObj o, boolean r) {
        WnContext wc = Wn.WC();
        o = wc.doHook("before_delete", o);
        io.delete(o, r);
        WnObj o2 = wc.doHook("delete", o);
        o.update2(o2);
    }

}
