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
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.AbstractWnTree;
import org.nutz.lang.util.Context;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoWnTree extends AbstractWnTree {

    private ZMoCo co;

    public MongoWnTree(WnTreeFactory factory, ZMoCo co) {
        super(factory);
        this.co = co;
    }

    @Override
    public WnNode _get_my_node(String id) {
        ZMoDoc q = WnMongos.qID(id);
        ZMoDoc doc = co.findOne(q);
        if (null != doc) {
            return WnMongos.toWnNode(doc);
        }
        return null;
    }

    protected WnNode _get_my_parent(WnNode nd) {
        MongoWnNode mnd = (MongoWnNode) nd;
        return getNode(mnd.parentId());
    }

    @Override
    public int eachMountTree(Each<WnTree> callback) {
        if (null == callback)
            return 0;

        ZMoDoc q = ZMoDoc.NEW().ne("mnt", null);

        DBCursor cu = co.find(q).sort(ZMoDoc.NEW("nm", 1));
        try {
            int i = 0;
            int n = 0;

            while (cu.hasNext()) {
                DBObject dbobj = cu.next();
                WnNode nd = WnMongos.toWnNode(dbobj);
                nd.setTree(this);

                WnTree tree = factory().check(nd.path(), nd.mount());

                // 虽然不太可能，但是还是判断一下防止无穷递归吧。
                if (tree == this)
                    continue;

                // 调用回调并计数
                try {
                    callback.invoke(i++, tree, n);
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
    protected WnNode _fetch_one_by_name(WnNode p, String name) {
        ZMoDoc q = ZMoDoc.NEW("pid", p.id()).putv("nm", name);
        ZMoDoc doc = co.findOne(q);
        return WnMongos.toWnNode(doc);
    }

    @Override
    public boolean exists(WnNode p, String name) {
        ZMoDoc q = ZMoDoc.NEW("pid", p.id()).putv("nm", name);
        return co.count(q) > 0;
    }

    @Override
    protected int _each_children(WnNode p, String str, Each<WnNode> callback) {
        if (null == callback)
            return 0;

        ZMoDoc q = ZMoDoc.NEW("pid", p.id());

        // 设置名称过滤条件
        if (!Strings.isBlank(str)) {
            // 本身就是正则
            if (str.startsWith("^")) {
                q.put("nm", Pattern.compile(str));
            }
            // 看看是通配符还是普通名字
            else {
                String s = str.replace("*", ".*");
                // 直接的名字
                if (s.equals(str)) {
                    q.put("nm", str);
                }
                // 通配符
                else {
                    q.put("nm", Pattern.compile("^" + s));
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
    public WnNode _create_node(WnNode p, String id, String name, WnRace race) {
        // 创建子节点
        MongoWnNode mnd = new MongoWnNode();
        if (Strings.isBlank(id))
            mnd.genID();
        else
            mnd.id(id);
        mnd.setParent(p);
        mnd.parentId(p.id());
        mnd.name(name);
        mnd.race(race);
        mnd.setTree(this);

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
    protected void _delete_self(WnNode nd) {
        co.remove(WnMongos.qID(nd.id()));
    }

    @Override
    public void _do_rename(WnNode nd, String newName) {
        MongoWnNode mnd = (MongoWnNode) nd;
        mnd.name(newName);
        ZMoDoc q = WnMongos.qID(mnd.id());
        ZMoDoc doc = ZMoDoc.NEW("nm", newName);
        co.update(q, doc);
    }

    @Override
    public WnNode _do_append(WnNode p, WnNode nd) {
        // 调用父类的检查
        super.append(p, nd);

        // 开始移动
        MongoWnNode mp = (MongoWnNode) p;
        MongoWnNode mnd = (MongoWnNode) nd;

        ZMoDoc q = WnMongos.qID(mnd.id());
        ZMoDoc doc = ZMoDoc.NEW("pid", mp.id());
        co.update(q, doc);

        mnd.parentId(mp.id());
        return mnd;
    }

    @Override
    public void setMount(WnNode nd, String mnt) {
        MongoWnNode mnd = (MongoWnNode) nd;
        mnd.mount(mnt);
        ZMoDoc q = WnMongos.qID(mnd.id());
        ZMoDoc doc = ZMoDoc.NEW();
        doc.set("mnt", mnt);

        co.update(q, doc);
    }

    @Override
    protected void _flush_buffer() {}

    @Override
    public void _clean_for_unit_test() {
        co.remove(ZMoDoc.NEW());
    }

}
