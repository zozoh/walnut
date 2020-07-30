package org.nutz.walnut.core.indexer;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.util.Wn;

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
    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode) {
        // DEPTH_LEAF_FIRST
        if (WalkMode.DEPTH_LEAF_FIRST == mode) {
            __walk_DEPTH_LEAF_FIRST(p, callback);
        }
        // DEPTH_NODE_FIRST
        else if (WalkMode.DEPTH_NODE_FIRST == mode) {
            __walk_DEPATH_NODE_FIRST(p, callback);
        }
        // 广度优先
        else if (WalkMode.BREADTH_FIRST == mode) {
            __walk_BREADTH_FIRST(p, callback);
        }
        // 仅叶子节点
        else if (WalkMode.LEAF_ONLY == mode) {
            __walk_LEAF_ONLY(p, callback);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }
    }

    protected void _do_walk_children(WnObj p, final Callback<WnObj> callback) {
        Each<WnObj> looper = Wn.eachLooping(new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                callback.invoke(obj);
            }
        });
        this.eachChild(p, null, looper);
    }

    private void __walk_LEAF_ONLY(WnObj p, final Callback<WnObj> callback) {
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                if (nd.isFILE())
                    callback.invoke(nd);
                else
                    __walk_LEAF_ONLY(nd, callback);
            }
        });
    }

    private void __walk_BREADTH_FIRST(WnObj p, final Callback<WnObj> callback) {
        final List<WnObj> list = new LinkedList<WnObj>();
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                callback.invoke(nd);
                if (!nd.isFILE())
                    list.add(nd);
            }
        });
        for (WnObj nd : list)
            __walk_BREADTH_FIRST(nd, callback);
    }

    private void __walk_DEPATH_NODE_FIRST(WnObj p, final Callback<WnObj> callback) {
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                callback.invoke(nd);
                if (!nd.isFILE()) {
                    __walk_DEPATH_NODE_FIRST(nd, callback);
                }
            }
        });
    }

    private void __walk_DEPTH_LEAF_FIRST(WnObj p, final Callback<WnObj> callback) {
        _do_walk_children(p, new Callback<WnObj>() {
            public void invoke(WnObj nd) {
                if (!nd.isFILE()) {
                    __walk_DEPTH_LEAF_FIRST(nd, callback);
                }
                callback.invoke(nd);
            }
        });
    }
}
