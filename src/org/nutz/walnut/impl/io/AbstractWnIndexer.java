package org.nutz.walnut.impl.io;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.MimeMap;
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
        return Wn.WC().whenAccess(o);
    }

    protected abstract WnObj _get(String id);

    @Override
    public int each(WnQuery q, final Each<WnObj> callback) {
        final WnContext wc = Wn.WC();
        final int[] re = new int[1];
        return _each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                o = wc.whenView(o);
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
        o = Wn.WC().whenAccess(o);
        _remove(o);
    }

    protected abstract void _remove(WnObj o);

    @Override
    public WnObj toObj(WnNode nd) {
        if (null == nd)
            return null;

        if (nd instanceof WnObj)
            return (WnObj) nd;

        WnObj o = get(nd.id());
        if (null == o) {
            WnContext wc = Wn.WC();
            
            o = new WnBean().setNode(nd);
            o.mender(wc.checkMe());
            o.group(wc.checkGroup());
            Wn.set_type(mimes, o, null);
            
        } else {
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
