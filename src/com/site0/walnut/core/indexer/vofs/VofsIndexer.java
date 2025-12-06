package com.site0.walnut.core.indexer.vofs;

import org.nutz.lang.Each;
import org.nutz.lang.util.Disks;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.core.indexer.AbstractIoVfsIndexer;
import com.site0.walnut.ext.xo.bean.XoBean;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class VofsIndexer extends AbstractIoVfsIndexer {

    // private static Log log = Wlog.getIO();

    private XoService api;

    public VofsIndexer(WnObj oMntRoot, MimeMap mimes, XoService xos) {
        super(oMntRoot, mimes);
        this.api = xos;
    }

    @Override
    public WnObj get(String id) {
        String path = get_path_by_id(id);

        // 直接获取
        XoBean xo = api.getObj(path);
        if (null != xo) {
            return wrap_vo(xo);
        }

        // 那就是找不到了
        return null;
    }

    private WnVofsObj wrap_vo(XoBean xo) {
        return new WnVofsObj(this.root, api, mimes, xo);
    }

    private WnVofsObj assert_vo(WnObj o) {
        if (null == o)
            return null;
        if (o instanceof WnVofsObj) {
            WnVofsObj vo = (WnVofsObj) o;
            if (!vo.oRoot.isSameId(getRootId())) {
                throw Er.create("e.io.vofs.NotInMyRoot", o.toString());
            }
            return vo;
        }

        throw Er.create("e.io.vofs.assertVofsObjFailed", o.toString());
    }

    String get_query_prefix(WnObj p, String path) {
        boolean p_is_root = null == p || this.isRoot(p);
        // 如果确定了本前缀是一个目录路径,那么输出的前缀需要以 "/" 结尾
        // 有下面几种情况,本前缀应该是目录
        // 1. p.isDIR && null==path
        // 2. p.isDIR && path.endsWith("/")
        boolean is_for_dir = p_is_root ? true : p.isDIR();

        if (null != path && !path.endsWith("/")) {
            is_for_dir = false;
        }

        // 获取起始点
        String rph = null;
        if (!p_is_root) {
            rph = Disks.getRelativePath(root.path(), p.path(), "");
        }

        // 归一化相对路径 (""|null) => null
        if (Ws.isBlank(rph)) {
            rph = null;
        }

        if (Ws.isBlank(path)) {
            path = rph;
        }
        // 这个前缀路径加上 path 就是相对根的绝对路径了
        else if (!Ws.isBlank(rph)) {
            path = Wn.appendPath(rph, path);
        }

        // 防空
        if (Ws.isBlank(path))
            return null;

        // 搜索前缀,有必要以 "/" 为结尾
        if (is_for_dir && !path.endsWith("/")) {
            path += "/";
        }

        // 搜素前缀一定不能以 "/" 开头
        if (path.startsWith("/")) {
            path = path.substring(1).trim();
        }

        return path;
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        // 得到查找的前缀
        String prefix = get_query_prefix(p, path);

        XoBean xo = api.getObj(prefix);
        if (null != xo) {
            return wrap_vo(xo);
        }

        // 那就是找不到了
        return null;
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        // 得到新路径相对于根的路径
        String newPath = Disks.getRelativePath(src.path(), destPath, "");

        // 无需移动
        if (Ws.isBlank(newPath)) {
            return src;
        }

        // 执行移动
        WnVofsObj vo = assert_vo(src);
        String key = vo.xo.getKey();

        // 确保结尾
        if (src.isDIR() && !newPath.endsWith("/")) {
            newPath += "/";
        } else if (src.isFILE() && newPath.endsWith("/")) {
            newPath += src.name();
        }

        api.renameObj(key, newPath);
        XoBean xo = api.getObj(newPath);
        make_sure_all_parents_exists(xo.getKey());
        return wrap_vo(xo);
    }

    @Override
    public WnObj move(WnObj src, String destPath, int mode) {
        // 得到新路径相对于根的路径
        String newPath = Disks.getRelativePath(root.path(), destPath, "");

        // 无需移动
        if (Ws.isBlank(newPath)) {
            return src;
        }

        // 执行移动
        WnVofsObj vo = assert_vo(src);
        String key = vo.xo.getKey();

        // 确保结尾
        if (src.isDIR() && !newPath.endsWith("/")) {
            newPath += "/";
        } else if (src.isFILE() && newPath.endsWith("/")) {
            newPath += src.name();
        }

        api.renameObj(key, newPath);
        XoBean xo = api.getObj(newPath);
        make_sure_all_parents_exists(xo.getKey());
        return wrap_vo(xo);
    }

    @Override
    public WnObj rename(WnObj o, String nm, int mode) {
        // 执行移动
        WnVofsObj vo = assert_vo(o);
        String key = vo.xo.getKey();

        String newKey = api.renameKey(key, nm);
        XoBean xo = api.getObj(newKey);
        return wrap_vo(xo);
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        // p 相对根，是个什么路径呢？
        String prefix = get_query_prefix(p, path);

        String key = path;
        // 这个前缀路径加上 path 就是相对根的绝对路径了
        if (null != prefix) {
            key = Wn.appendPath(prefix);
        }

        // 确保前缀不是 /
        if (key.startsWith("/")) {
            key = key.substring(1).trim();
        }

        // 确保结尾
        if (WnRace.DIR == race && !key.endsWith("/")) {
            key += "/";
        } else if (WnRace.FILE == race && key.endsWith("/")) {
            key = key.substring(0, key.length() - 1).trim();
        }

        // 创建对象
        api.writeText(key, "", null);
        XoBean xo = api.getObj(key);
        make_sure_all_parents_exists(key);

        // 最后返回
        return wrap_vo(xo);
    }

    private void make_sure_all_parents_exists(String key) {
        // 确保创建所有的父
        String pkey = WnVofsObj.getParentKey(key);
        while (null != pkey) {
            if (null == api.getObj(pkey)) {
                api.mkdir(pkey);
            }
            pkey = WnVofsObj.getParentKey(pkey);
        }
    }

    @Override
    public void delete(WnObj o) {
        // 防空
        if (null == o)
            return;
        WnVofsObj vo = assert_vo(o);
        String key = vo.xo.getKey();
        api.deleteObj(key);
    }

    @Override
    public int each(WnQuery q, Each<WnObj> callback) {
        String prefix = q.first().getString("prefix", null);

        if (null == prefix) {
            String pid = q.first().getString("pid");
            // 用根遍历
            if (this.isRoot(pid)) {
                prefix = null;
            }
            // 指定了一个父
            else if (!Ws.isBlank(pid)) {
                WnObjId oid = new WnObjId(pid);
                prefix = get_path_by_id(oid.getMyId());
            }
        }

        return api.eachObj(prefix, (i, xo, len) -> {
            if (null != callback) {
                WnVofsObj vo = wrap_vo(xo);
                callback.invoke(i, vo, len);
            }
        });
    }

    @Override
    public int eachChild(WnObj o, String name, Each<WnObj> callback) {
        String prefix = get_query_prefix(o, name);
        return api.eachObj(prefix, (i, xo, len) -> {
            if (null != callback) {
                WnVofsObj vo = wrap_vo(xo);
                callback.invoke(i, vo, len);
            }
        });
    }

    @Override
    public int countChildren(WnObj o) {
        return eachChild(o, null, null);
    }

}
