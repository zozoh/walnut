package com.site0.walnut.core.indexer;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Strings;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.Callback;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WalkMode;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnObjFilter;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.util.Wn;

public abstract class AbstractIoIndexer implements WnIoIndexer {

    protected WnObj root;

    // protected WnIoMappingFactory mappings;

    protected MimeMap mimes;

    protected AbstractIoIndexer(WnObj root, MimeMap mimes) {
        this.root = root;
        this.mimes = mimes;
    }

    @Override
    public WnObj getRoot() {
        return root;
    }

    @Override
    public String getRootId() {
        return root.id();
    }

    @Override
    public boolean isRoot(String id) {
        return root.isSameId(id);
    }

    @Override
    public boolean isRoot(WnObj o) {
        return root.isSameId(o);
    }

    @Override
    public MimeMap mimes() {
        return mimes;
    }

    @Override
    public boolean exists(WnObj p, String ph) {
        WnObj o = this.fetch(p, ph);
        return o != null;
    }

    @Override
    public boolean existsId(String id) {
        WnObj o = get(id);
        return null != o;
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
        WnObj o = this.fetch(p, path);
        if (null == o) {
            throw Er.create("e.io.obj.noexists", path);
        }
        return o;
    }

    @Override
    public WnObj fetch(WnObj p,
                       String[] paths,
                       boolean isForDir,
                       int fromIndex,
                       int toIndex) {
        int len = toIndex - fromIndex;
        String path = Strings.join(fromIndex, len, "/", paths);
        if (isForDir) {
            path += "/";
        }
        return fetch(p, path);
    }

    @Override
    public WnObj fetchByName(WnObj p, String name) {
        return this.fetch(p, name);
    }

    @Override
    public List<WnObj> getChildren(WnObj o, String name) {
        List<WnObj> list = new LinkedList<>();
        this.eachChild(o, name, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) {
                list.add(ele);
            }
        });
        return list;
    }

    @Override
    public boolean hasChild(WnObj p) {
        return countChildren(p) > 0;
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        List<WnObj> list = new LinkedList<>();
        this.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                list.add(o);
            }
        });
        return list;
    }

    @Override
    public void walk(WnObj p,
                     Callback<WnObj> callback,
                     WalkMode mode,
                     WnObjFilter filter) {
        // DEPTH_LEAF_FIRST
        if (WalkMode.DEPTH_LEAF_FIRST == mode) {
            __walk_DEPTH_LEAF_FIRST(p, callback, filter);
        }
        // DEPTH_NODE_FIRST
        else if (WalkMode.DEPTH_NODE_FIRST == mode) {
            __walk_DEPATH_NODE_FIRST(p, callback, filter);
        }
        // 广度优先
        else if (WalkMode.BREADTH_FIRST == mode) {
            __walk_BREADTH_FIRST(p, callback, filter);
        }
        // 仅叶子节点
        else if (WalkMode.LEAF_ONLY == mode) {
            __walk_LEAF_ONLY(p, callback, filter);
        }
        // 不可能
        else {
            throw Wlang.impossible();
        }
    }

    protected void _do_walk_children(WnObj p,
                                     final Callback<WnObj> callback,
                                     WnObjFilter filter) {
        if (null != filter && null != p && !filter.match(p)) {
            return;
        }
        Each<WnObj> looper = Wn.eachLooping(new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                callback.invoke(obj);
            }
        });
        this.eachChild(p, null, looper);
    }

    private void __walk_LEAF_ONLY(WnObj p,
                                  final Callback<WnObj> callback,
                                  WnObjFilter filter) {
        _do_walk_children(p, (WnObj nd) -> {
            if (nd.isFILE())
                callback.invoke(nd);
            else
                __walk_LEAF_ONLY(nd, callback, filter);

        }, filter);
    }

    private void __walk_BREADTH_FIRST(WnObj p,
                                      final Callback<WnObj> callback,
                                      WnObjFilter filter) {
        final List<WnObj> list = new LinkedList<WnObj>();
        _do_walk_children(p, (WnObj nd) -> {
            callback.invoke(nd);
            if (!nd.isFILE())
                list.add(nd);
        }, filter);
        for (WnObj nd : list)
            __walk_BREADTH_FIRST(nd, callback, filter);
    }

    private void __walk_DEPATH_NODE_FIRST(WnObj p,
                                          final Callback<WnObj> callback,
                                          WnObjFilter filter) {
        _do_walk_children(p, (WnObj nd) -> {
            callback.invoke(nd);
            if (!nd.isFILE()) {
                __walk_DEPATH_NODE_FIRST(nd, callback, filter);
            }
        }, filter);
    }

    private void __walk_DEPTH_LEAF_FIRST(WnObj p,
                                         final Callback<WnObj> callback,
                                         WnObjFilter filter) {
        _do_walk_children(p, (WnObj nd) -> {
            if (!nd.isFILE()) {
                __walk_DEPTH_LEAF_FIRST(nd, callback, filter);
            }
            callback.invoke(nd);
        }, filter);
    }

    @Override
    public WnObj getOne(WnQuery q) {
        q.limit(1);
        List<WnObj> list = query(q);
        if (list.isEmpty())
            return null;
        return list.get(0);
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        return this.move(src, destPath, Wn.MV.TP | Wn.MV.SYNC);
    }

    @Override
    public WnObj rename(WnObj o, String nm) {
        return this.rename(o, nm, false);
    }

    @Override
    public WnObj rename(WnObj o, String nm, boolean keepType) {
        int mode = Wn.MV.SYNC;
        if (!keepType)
            mode |= Wn.MV.TP;
        return this.rename(o, nm, mode);
    }
}
