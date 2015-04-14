package org.nutz.walnut.impl.mongo;

import java.util.regex.Pattern;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.AbstractWnTree;
import org.nutz.lang.util.Context;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoWnTree extends AbstractWnTree {

    private ZMoCo co;

    public MongoWnTree(WnTreeFactory factory, WnNode treeNode, ZMoCo co) {
        super(factory, treeNode);
        this.co = co;
    }

    @Override
    public void _clean_for_unit_test() {
        co.remove(ZMoDoc.NEW());
    }

    @Override
    public WnNode getNode(String id) {
        if (treeNode.isSameId(id))
            return treeNode;
        ZMoDoc q = WnMongos.qID(id);
        ZMoDoc doc = co.findOne(q);
        if (null != doc) {
            WnNode nd = WnMongos.toWnNode(doc);
            return loadParents(nd, false);
        }
        return null;
    }

    @Override
    public WnNode loadParents(WnNode nd, boolean force) {
        if (treeNode.isSameId(nd)) {
            if (treeNode != nd) {
                nd.setParent(treeNode.parent());
            }
            return nd;
        }
        if (null == nd.parent() || force) {
            MongoWnNode mnd = (MongoWnNode) nd;
            WnNode p = getNode(mnd.parentId());
            loadParents(p, force);
            mnd.setParent(p);
        }
        return nd;
    }

    @Override
    public int eachChildren(WnNode p, String str, Each<WnNode> callback) {
        if (null == callback)
            return 0;

        WnNode pnd = null == p ? treeNode : p;

        ZMoDoc q = ZMoDoc.NEW().set("pid", pnd.id());

        // 设置名称过滤条件
        if (!Strings.isBlank(str)) {
            // 本身就是正则
            if (str.startsWith("^")) {
                q.set("nm", Pattern.compile(str));
            }
            // 看看是通配符还是普通名字
            else {
                String s = str.replace("*", ".*");
                // 直接的名字
                if (s.equals(str)) {
                    q.set("nm", str);
                }
                // 通配符
                else {
                    q.set("nm", Pattern.compile("^" + s));
                }
            }
        }

        DBCursor cu = co.find(q).sort(ZMoDoc.NEW("nm", 1));
        try {
            int i = 0;
            int n = 0;

            while (cu.hasNext()) {
                DBObject dbobj = cu.next();
                WnNode nd = WnMongos.toWnNode(dbobj);

                _fill_parent_full_path(p, nd);

                // 调用回调并计数
                try {
                    callback.invoke(i++, nd, n);
                }
                catch (ExitLoop e) {
                    break;
                }
                catch (ContinueLoop e) {}
                finally {
                    n++;
                }
            }

            return n;
        }
        finally {
            cu.close();
        }
    }

    @Override
    public boolean hasChildren(WnNode nd) {
        ZMoDoc doc = co.findOne(ZMoDoc.NEW("pid", nd.id()));
        return null != doc;
    }

    @Override
    public WnNode create_node(WnNode p, String name, WnRace race) {
        p = check_parent(p, race);

        MongoWnNode mnd = new MongoWnNode();
        mnd.genID();
        mnd.setParent(p);
        mnd.parentId(p.id()).name(name).race(race).setTree(this);

        // 展开名字
        Segment seg = Segments.create(mnd.name());
        if (seg.hasKey()) {
            Context c = Lang.context();
            c.set("id", mnd.id());
            String newName = seg.render(c).toString();
            mnd.name(newName);
        }

        // 检查同名
        if (null != mnd.parentId()) {
            ZMoDoc q = ZMoDoc.NEW("pid", mnd.parentId());
            q.put("nm", mnd.name());
            if (null != co.findOne(q)) {
                throw Er.create("e.io.tree.nd.exists", mnd);
            }
        }

        // 开始创建
        ZMoDoc doc = ZMo.me().toDoc(mnd).genID();
        co.save(doc);

        // 返回
        return mnd;
    }

    @Override
    protected void delete_self(WnNode nd) {
        co.remove(WnMongos.qID(nd.id()));
    }

    @Override
    public void rename(WnNode nd, String newName) {
        MongoWnNode mnd = (MongoWnNode) nd;
        mnd.name(newName);
        ZMoDoc q = WnMongos.qID(mnd.id());
        ZMoDoc doc = ZMoDoc.NEW("nm", newName);
        co.update(q, doc);
    }

    @Override
    public void setMount(WnNode nd, String mnt) {
        MongoWnNode mnd = (MongoWnNode) nd;
        mnd.mount(mnt);
        ZMoDoc q = WnMongos.qID(mnd.id());
        ZMoDoc doc = ZMoDoc.NEW("mnt", mnt);
        co.update(q, doc);
    }

    @Override
    protected void _flush_buffer() {}

}
