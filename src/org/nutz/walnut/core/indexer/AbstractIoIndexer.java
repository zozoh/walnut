package org.nutz.walnut.core.indexer;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.core.WnIoIndexer;
import org.nutz.walnut.util.Wn;

public abstract class AbstractIoIndexer implements WnIoIndexer {

    protected WnObj root;

    protected MimeMap mimes;

    protected AbstractIoIndexer(WnObj root, MimeMap mimes) {
        this.root = root;
        this.mimes = mimes;
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
        WnObj o = fetch(p, path);
        if (null == o)
            throw Er.create("e.io.obj.noexists", path);
        return o;
    }

    @Override
    public boolean existsId(String id) {
        WnQuery q = Wn.Q.id(id);
        long re = this.count(q);
        return re > 0;
    }

    @Override
    public List<WnObj> query(WnQuery q) {
        final List<WnObj> list = new LinkedList<WnObj>();
        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                list.add(obj);
            }
        });
        return list;
    }

    @Override
    public WnObj rename(WnObj o, String nm) {
        return rename(o, nm, false);
    }

    @Override
    public WnObj rename(WnObj o, String nm, boolean keepType) {
        int mode = Wn.MV.SYNC;
        if (!keepType)
            mode |= Wn.MV.TP;
        return rename(o, nm, mode);
    }

    @Override
    public WnObj rename(WnObj o, String nm, int mode) {
        String ph = o.path();
        ph = Files.renamePath(ph, nm);
        return move(o, ph, mode);
    }

    @Override
    public WnObj move(WnObj src, String destPath) {
        return move(src, destPath, Wn.MV.TP | Wn.MV.SYNC);
    }

    @Override
    public WnObj setBy(String id, NutMap map, boolean returnNew) {
        return setBy(Wn.Q.id(id), map, returnNew);
    }

    @Override
    public int inc(String id, String key, int val, boolean returnNew) {
        return this.inc(Wn.Q.id(id), key, val, returnNew);
    }

    @Override
    public WnObj getRoot() {
        return this.root;
    }

    @Override
    public String getRootId() {
        return this.root.id();
    }

    @Override
    public boolean isRoot(String id) {
        return this.root.isSameId(id);
    }

    @Override
    public boolean isRoot(WnObj o) {
        return root.isSameId(o);
    }
    
    @Override
    public WnObj getOne(WnQuery q) {
        final WnObj[] re = new WnObj[1];
        if (q == null)
            q = new WnQuery();
        q.limit(1);

        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                re[0] = obj;
            }
        });
        return re[0];
    }

    @Override
    public void walk(WnObj p, final Callback<WnObj> callback, final WalkMode mode) {
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
        List<WnObj> list = this.getChildren(p, null);
        for (WnObj o : list) {
            try {
                callback.invoke(o);
            }
            catch (ExitLoop e) {
                break;
            }
            catch (ContinueLoop e) {
                continue;
            }
        }
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
