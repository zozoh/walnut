package org.nutz.walnut.impl.io;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.ObjIndexStrategy;
import org.nutz.walnut.api.io.WnIndexer;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public abstract class AbstractWnIndexer implements WnIndexer {

    private MimeMap mimes;

    @Override
    public WnObj get(String id) {
        WnObj o = _get(id);
        if (null == o)
            return null;
        return toObj(Wn.WC().whenAccess(o), ObjIndexStrategy.PARENT);
    }

    protected abstract WnObj _get(String id);

    @Override
    public int each(WnQuery q, final Each<WnObj> callback) {
        final WnContext wc = Wn.WC();
        final int[] re = new int[1];
        return _each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                o = toObj(wc.whenView(o), ObjIndexStrategy.PARENT);
                if (null != o) {
                    callback.invoke(re[0]++, o, -1);
                }
            }
        });
    }

    protected abstract int _each(WnQuery q, Each<WnObj> callback);

    @Override
    public void remove(String id) {
        WnObj o = _get(id);
        if (null != o) {
            o = toObj(Wn.WC().whenRemove(o), ObjIndexStrategy.IMPLICT);
            _remove(o);
        }
    }

    protected abstract void _remove(WnObj o);

    @Override
    public WnObj toObj(WnNode nd, ObjIndexStrategy ois) {
        if (null == nd)
            return null;

        if (nd instanceof WnObj)
            return (WnObj) nd;

        // 默认采用严格模式
        if (null == ois)
            ois = ObjIndexStrategy.STRICT;

        WnObj o = _get(nd.id());
        if (null == o) {
            // 严格模式，不能允许索引为空
            if (ObjIndexStrategy.STRICT == ois) {
                throw Er.create("e.obj.noindex", nd);
            }

            // 准备父对象变量，下面可能会用得到
            WnObj p = null;

            o = new WnBean().setNode(nd);
            Wn.set_type(mimes, o, null);
            o.createTime(o.nanoStamp() / 1000000L);

            // 跟随父节点设置
            WnNode pNode = nd.parent();

            // 顶级节点，均属于 root
            if (null == pNode) {
                o.creator("root").mender("root").group("root");
            }
            // 跟随父节点设置
            else if (ObjIndexStrategy.PARENT == ois) {
                p = toObj(pNode, ois);
                o.creator(p.creator()).mender(p.mender()).group(p.group());
            }
            // 设置创建者，以及权限相关
            else if (ObjIndexStrategy.WC == ois) {
                WnContext wc = Wn.WC();
                String g = wc.checkGroup();
                String c = wc.checkMe();
                o.creator(c).mender(c).group(g);
            }

            // 计算 d0,d1
            String ph = nd.path();
            String[] ss = Strings.splitIgnoreBlank(ph, "/");
            for (int i = 0; i < ss.length; i++) {
                o.put("d" + i, ss[i]);
            }

            // 主节点和 home 必须是可以进入的
            if (ss.length == 0 || (ph.equals("/home"))) {
                o.mode(0755);
            }
            // 二级节点参照父
            else if (ss.length == 2) {
                if (null == p)
                    p = toObj(pNode, ois);
                o.mode(p.mode());
            }
            // 其他的节点统统保护 >o<
            else {
                o.mode(0750);
            }
            // 保存到元数据表
            if (ObjIndexStrategy.IMPLICT != ois) {
                this.set(o, "^d[0-9]|lm|ct|nano|tp|mime|c|m|g|md$");
            }

        }
        // 找到了索引，填充节点信息
        else {
            o.setNode(nd);
        }
        return o;
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
    public WnObj getOne(WnQuery q) {
        final WnObj[] re = new WnObj[1];
        if (null != q)
            q.limit(1);
        each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                re[0] = obj;
                Lang.Break();
            }
        });
        return re[0];
    }

    @Override
    public void _clean_for_unit_test() {}

    @Override
    public void set(WnObj o, String regex) {
        _set(o.id(), o.toMap4Update(regex));
    }

    protected abstract void _set(String id, NutMap map);

}
