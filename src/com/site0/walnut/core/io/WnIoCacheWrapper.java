package com.site0.walnut.core.io;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.WnIoMappingFactory;
import com.site0.walnut.core.cache.WnIoCache;
import com.site0.walnut.util.Wn;

public class WnIoCacheWrapper extends AbstractWnIoWrapper {

    private WnIoCache cache;

    private void __init_dft_fields() {
        this.cache = new WnIoCache();
        this.cache.objDuInSec = 3;
        this.cache.objCleanThreshold = 1000;
        this.cache.sha1DuInSec = 3600;
        this.cache.sha1CleanThreshold = 1000;
        this.cache.ready();
    }

    public WnIoCacheWrapper() {
        __init_dft_fields();
    }

    public WnIoCacheWrapper(WnIo io) {
        super(io);
        __init_dft_fields();
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    @Override
    public WnLockApi getLockApi() {
        return io.getLockApi();
    }

    @Override
    public WnIoMappingFactory getMappingFactory() {
        return io.getMappingFactory();
    }

    @Override
    public WnObj get(String id) {
        // 尝试命中
        WnObj o = cache.getObjById(id);
        if (null != o) {
            if (o.isExpired()) {
                this.delete(o);
                o = null;
            } else {
                return Wn.WC().whenAccess(o, true);
            }
        }

        o = super.get(id);

        // 计入缓冲
        cache.cacheObj(o);

        // 返回
        return o;
    }

    @Override
    public WnObj checkById(String id) {
        WnObj o = this.get(id);
        if (null == o) {
            throw Er.create("e.io.obj.noexists", "id:" + id);
        }
        return o;
    }

    @Override
    public WnObj check(WnObj p, String path) {
        WnObj o = __fetch_obj_by_path(p, path, false);
        if (null == o)
            if (null == p) {
                throw Er.create("e.io.obj.noexists", path);
            } else {
                throw Er.create("e.io.obj.noexists", path + ", p:" + p.path());
            }
        return o;
    }

    private WnObj __fetch_obj_by_path(WnObj p, String path, boolean asNull) {
        // System.out.printf("cacheIo.fetch: p=%s, path=%s\n", p, path);
        // 尝试命中
        WnObj o = null;
        if (null != path && path.startsWith("id:") && !path.contains("/")) {
            String oid = path.substring(3).trim();
            return this.get(oid);
        }

        if (null == p) {
            o = cache.fetchByPath(path);
            // System.out.printf("cacheIo.fetch: pathCache matched o=%s\n", o);
            if (null != o) {
                if (o.isExpired()) {
                    this.delete(o);
                    o = null;
                } else {
                    return Wn.WC().whenAccess(o, asNull);
                }
            }
        }
        o = super.fetch(p, path);

        // 计入缓冲
        if (null == p) {
            cache.cacheObj(o);
        }

        // 返回
        return o;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        return __fetch_obj_by_path(p, path, true);
    }

    @Override
    public String readText(WnObj o) {
        byte[] bs = readBytes(o);
        return new String(bs, Encoding.CHARSET_UTF8);
    }

    @Override
    public <T> T readJson(WnObj o, Class<T> classOfT) {
        String json = readText(o);
        return Json.fromJson(classOfT, json);
    }

    @Override
    public BufferedImage readImage(WnObj o) {
        InputStream ins = null;
        try {
            ins = this.getInputStream(o, 0);
            return ImageIO.read(ins);
        }
        catch (IOException e) {
            throw Er.create(e, "e.io.read.img", o);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    @Override
    public byte[] readBytes(WnObj o) {
        // 检查权限
        o = Wn.WC().whenRead(o, false);

        // 如果是小文本文件，则尝试命中缓存
        long olen = o.len();
        String mime = o.mime();
        String sha1 = o.sha1();
        boolean is_small_text_file = olen < 1000000L
                                     && o.isFILE()
                                     && null != mime
                                     && mime.startsWith("text/");

        // 尝试命中
        if (is_small_text_file) {
            byte[] re = cache.getBytes(sha1);
            if (null == re) {
                re = super.readBytes(o);
                cache.cacheContent(sha1, re);
            }

            return re;
        }

        // 采用父类方法，并且不缓冲
        return super.readBytes(o);
    }

    @Override
    public InputStream getInputStream(WnObj o, long off) {
        // 检查权限
        o = Wn.WC().whenRead(o, false);

        // 如果是小文本文件，则尝试命中缓存
        long olen = o.len();
        String mime = o.mime();
        String sha1 = o.sha1();
        boolean is_small_text_file = olen < 1000000L
                                     && o.isFILE()
                                     && null != mime
                                     && mime.startsWith("text/");

        // 尝试命中
        if (is_small_text_file) {
            byte[] re = cache.getBytes(sha1);
            if (null == re) {
                re = super.readBytes(o);
                cache.cacheContent(sha1, re);
            }

            return new ByteInputStream(re);
        }

        // 采用父类方法，并且不缓冲
        return super.getInputStream(o, off);
    }

    @Override
    public void appendMeta(WnObj o, Object meta, boolean keepType) {
        io.appendMeta(o, meta);
        cache.removeFromCache(o);
    }

    @Override
    public void appendMeta(WnObj o, Object meta) {
        super.appendMeta(o, meta);
        cache.removeFromCache(o);
    }

    @Override
    public WnObj setBy(String id, String key, Object val, boolean returnNew) {
        WnObj o = super.setBy(id, key, val, returnNew);
        cache.removeFromCache(o);
        return o;
    }

    @Override
    public WnObj setBy(WnQuery q, String key, Object val, boolean returnNew) {
        WnObj o = super.setBy(q, key, val, returnNew);
        cache.removeFromCache(o);
        return o;
    }

    @Override
    public void set(WnObj o, String regex) {
        super.set(o, regex);
        cache.removeFromCache(o);
    }

    @Override
    public WnObj setBy(String id, NutBean map, boolean returnNew) {
        WnObj o = super.setBy(id, map, returnNew);
        cache.removeFromCache(o);
        return o;
    }

    @Override
    public void setMount(WnObj o, String mnt) {
        super.setMount(o, mnt);
        cache.removeFromCache(o);
    }

    @Override
    public WnObj setBy(WnQuery q, NutBean map, boolean returnNew) {
        WnObj o = super.setBy(q, map, returnNew);
        cache.removeFromCache(o);
        return o;
    }

    @Override
    public long copyData(WnObj a, WnObj b) {
        long re = super.copyData(a, b);
        cache.removeFromCache(b);
        return re;
    }

    @Override
    public void writeMeta(WnObj o, Object meta) {
        cache.removeFromCache(o);
        super.writeMeta(o, meta);
    }

    @Override
    public long writeImage(WnObj o, RenderedImage im) {
        cache.removeFromCache(o);
        return super.writeImage(o, im);
    }

    @Override
    public long writeText(WnObj o, CharSequence cs) {
        cache.removeFromCache(o);
        return super.writeText(o, cs);
    }

    @Override
    public long writeJson(WnObj o, Object obj, JsonFormat fmt) {
        cache.removeFromCache(o);
        return super.writeJson(o, obj, fmt);
    }

    @Override
    public long writeBytes(WnObj o, byte[] buf) {
        cache.removeFromCache(o);
        return super.writeBytes(o, buf);
    }

    @Override
    public long writeBytes(WnObj o, byte[] buf, int off, int len) {
        cache.removeFromCache(o);
        return super.writeBytes(o, buf, off, len);
    }

    @Override
    public long write(WnObj o, InputStream ins) {
        cache.removeFromCache(o);
        return super.write(o, ins);
    }

    @Override
    public long writeAndClose(WnObj o, InputStream ins) {
        cache.removeFromCache(o);
        return super.writeAndClose(o, ins);
    }

    @Override
    public OutputStream getOutputStream(WnObj o, long off) {
        cache.removeFromCache(o);
        return super.getOutputStream(o, off);
    }

    @Override
    public WnObj pull(String id, String key, Object val, boolean returnNew) {
        WnObj o = cache.getObjById(id);
        cache.removeFromCache(o);
        return super.pull(id, key, val, returnNew);
    }

    @Override
    public void pull(WnQuery query, String key, Object val) {
        WnObj o = super.getOne(query);
        cache.removeFromCache(o);
        super.pull(query, key, val);
    }

    @Override
    public WnObj createIfNoExists(WnObj p, String path, WnRace race) {
        WnObj o = super.createIfNoExists(p, path, race);
        cache.removeFromCache(o.parent());
        return o;
    }

    @Override
    public WnObj createIfExists(WnObj p, String path, WnRace race) {
        WnObj o = super.createIfExists(p, path, race);
        cache.removeFromCache(o.parent());
        return o;
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        WnObj o = super.create(p, path, race);
        cache.removeFromCache(o.parent());
        return o;
    }

    @Override
    public WnObj create(WnObj p,
                        String[] paths,
                        int fromIndex,
                        int toIndex,
                        WnRace race) {
        WnObj o = super.create(p, paths, fromIndex, toIndex, race);
        cache.removeFromCache(o.parent());
        return o;
    }

    @Override
    public WnObj create(WnObj p, WnObj o) {
        WnObj o2 = super.create(p, o);
        cache.removeFromCache(o2.parent());
        return o2;
    }

    @Override
    public WnObj createIfNoExists(WnObj p, WnObj o) {
        WnObj o2 = super.createIfNoExists(p, o);
        cache.removeFromCache(o2.parent());
        return o2;
    }

    @Override
    public WnObj createIfExists(WnObj p, WnObj o) {
        WnObj o2 = super.createIfExists(p, o);
        cache.removeFromCache(o2.parent());
        return o2;
    }

    @Override
    public WnObj createById(WnObj p, String id, String name, WnRace race) {
        WnObj o2 = super.createById(p, id, name, race);
        cache.removeFromCache(o2.parent());
        return o2;
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        cache.removeFromCache(id);
        return super.inc(id, key, val, returnNew);
    }

    @Override
    public int inc(WnQuery q, String key, int val, boolean returnNew) {
        WnObj o = super.getOne(q);
        cache.removeFromCache(o);
        return super.inc(q, key, val, returnNew);
    }

    @Override
    public void delete(WnObj o) {
        super.delete(o);
        cache.removeFromCache(o);
    }

    @Override
    public void delete(WnObj o, boolean r) {
        super.delete(o, r);
        cache.removeFromCache(o);
    }

}
