package com.site0.walnut.core.cache;

import java.util.List;

import org.nutz.lang.Encoding;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.cache.WnCache;
import com.site0.walnut.cache.temp.WnInterimCache;
import com.site0.walnut.util.Ws;

public class WnIoCache extends WnIoCacheOptions {

    private WnCache<WnObj> idCache;

    private WnCache<WnObj> pathCache;

    private WnCache<byte[]> sha1Cache;

    public WnIoCache() {
        objDuInSec = 0;
        objCleanThreshold = 0;
        sha1DuInSec = 0;
        sha1CleanThreshold = 0;
    }

    public WnIoCache(WnIoCacheOptions options) {
        objDuInSec = options.objDuInSec;
        objCleanThreshold = options.objCleanThreshold;
        sha1DuInSec = options.sha1DuInSec;
        sha1CleanThreshold = options.sha1CleanThreshold;
        ready();
    }

    public void ready() {
        // 对象默认缓存 3 秒
        if (objDuInSec <= 0) {
            objDuInSec = 3;
        }

        // 对象默认超过千个清理
        if (objCleanThreshold <= 0) {
            objCleanThreshold = 1000;
        }

        // Sha1默认缓存 10 分钟
        if (sha1DuInSec <= 0) {
            sha1DuInSec = 600;
        }

        // Sha1默认超过千个清理
        if (sha1CleanThreshold <= 0) {
            sha1CleanThreshold = 1000;
        }

        idCache = new WnInterimCache<>(objDuInSec, objCleanThreshold);
        pathCache = new WnInterimCache<>(objDuInSec, objCleanThreshold);
        sha1Cache = new WnInterimCache<>(sha1DuInSec, sha1CleanThreshold);

        // SHA1 缓存应该是需要有获取更新过期时间机制的
        // 这样频繁访问的内容才更容易被访问到
        ((WnInterimCache<byte[]>) sha1Cache).setTouchWhenGet(true);
    }

    public WnObj getObjById(String id) {
        WnObj o = idCache.get(id);
        return __check_if_no_cached(o);
    }

    public WnObj fetchByPath(String path) {
        path = __tidy_path(path);
        WnObj o = pathCache.get(path);
        return __check_if_no_cached(o);
    }

    private String __tidy_path(String path) {
        if (null != path && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private WnObj __check_if_no_cached(WnObj o) {
        if (null != o && o.getBoolean("no_cached")) {
            idCache.remove(o.id());
            pathCache.remove(o.path());
        }
        return o;
    }

    public void cacheObj(WnObj o) {
        if (null == o) {
            return;
        }
        // 明确指定不缓存
        if (o.getBoolean("no_cached")) {
            return;
        }
        String oid = o.id();
        String opath = o.path();
        idCache.put(oid, o);
        pathCache.put(opath, o);
    }

    public void removeFromCache(WnObj o) {
        if (null == o) {
            return;
        }
        String oid = o.id();
        String opath = o.path();

        idCache.remove(oid);
        pathCache.remove(opath);

        // 链接目录
        if (o.isLink()) {
            String ln = o.link();
            removeFromCache(ln);
        }

        if (o.isFromLink()) {
            String ln = o.fromLink();
            removeFromCache(ln);
        }

        // 清理自己的祖先
        List<WnObj> ans = o.parents();
        for (WnObj an : ans) {
            // 设置了同步时间的祖先清除
            if (an.syncTime() > 0) {
                idCache.remove(an.id());
                pathCache.remove(an.path());
            }
            // 设置了链接目录的祖先，也清除
            else if (an.isFromLink()) {
                String ln = an.fromLink();
                removeFromCache(ln);
            }
        }
    }

    public void removeFromCache(String idPath) {
        // 防空
        if (Ws.isBlank(idPath)) {
            return;
        }

        String objId = null;
        String objPath = null;
        // 用 ID 作为路径
        if (idPath.startsWith("id:")) {
            String id = idPath.substring(3).trim();
            WnObj o = idCache.get(id);
            if (null != o) {
                objId = id;
                objPath = o.path();
            }
        }
        // 直接就是路径
        else {
            String path = idPath;
            path = __tidy_path(path);
            WnObj o = pathCache.get(path);
            if (null != o) {
                objId = o.id();
                objPath = o.path();
            }
        }
        if (null != objId)
            idCache.remove(objId);

        if (null != objPath)
            pathCache.remove(objPath);
    }

    public byte[] getBytes(String sha1) {
        return sha1Cache.get(sha1);
    }

    public String getContent(String sha1) {
        byte[] bs = sha1Cache.get(sha1);
        return new String(bs, Encoding.CHARSET_UTF8);
    }

    public void cacheContent(String sha1, String content) {
        if (null == content) {
            return;
        }
        byte[] bs = content.getBytes();
        sha1Cache.put(sha1, bs);
    }

    public void cacheContent(String sha1, byte[] bs) {
        if (null == bs) {
            return;
        }
        sha1Cache.put(sha1, bs);
    }

}
