package org.nutz.walnut.ext.thing;

import java.util.Collection;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.impl.CreateThingAction;
import org.nutz.walnut.ext.thing.impl.DeleteThingAction;
import org.nutz.walnut.ext.thing.impl.GetThingAction;
import org.nutz.walnut.ext.thing.impl.QueryThingAction;
import org.nutz.walnut.ext.thing.impl.UpdateThingAction;
import org.nutz.walnut.ext.thing.util.ThQr;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.util.WnPager;

public class WnThingService {

    private WnIo io;

    private WnObj oTs;

    public WnThingService(WnIo io, WnObj oTs) {
        this.io = io;
        this.oTs = oTs;
    }

    private <T extends ThingAction<?>> T _A(T a) {
        a.setIo(io).setThingSet(oTs);
        return a;
    }

    public WnObj getThing(String id, boolean isFull) {
        return _A(new GetThingAction()).setFull(isFull).setId(id).invoke();
    }

    public WnObj fetchThing(String th_nm, boolean isFull) {
        ThQuery tq = new ThQuery();
        tq.autoObj = true;
        tq.wp = new WnPager();
        tq.wp.limit = 1;
        tq.qStr = Json.toJson(Lang.map("th_nm", th_nm));
        List<WnObj> list = this.queryList(tq);
        return null != list && list.size() > 0 ? list.get(0) : null;
    }

    public WnObj getOne(ThQuery tq) {
        if (null == tq.wp) {
            tq.wp = new WnPager();
        }
        tq.wp.set(1, 0);
        List<WnObj> list = this.queryList(tq);
        if (list.isEmpty())
            return null;
        return list.get(0);
    }

    public WnObj createThing(String th_nm, NutMap meta) {
        return _A(new CreateThingAction()).setName(th_nm).setMeta(meta).invoke();
    }

    public ThQr queryThing(ThQuery tq) {
        return _A(new QueryThingAction()).setQuery(tq).invoke();
    }

    @SuppressWarnings("unchecked")
    public List<WnObj> queryList(ThQuery tq) {
        tq.autoObj = false;
        ThQr qr = this.queryThing(tq);
        if (null != qr.data)
            return (List<WnObj>) qr.data;
        return null;
    }

    public List<WnObj> deleteThing(boolean quiet, Collection<String> ids) {
        return _A(new DeleteThingAction()).setQuiet(quiet).setIds(ids).invoke();
    }

    public List<WnObj> deleteThing(boolean quiet, String... ids) {
        return _A(new DeleteThingAction()).setQuiet(quiet).setIds(Lang.list(ids)).invoke();
    }

    public WnObj updateThing(String id, NutMap meta, String th_nm) {
        return _A(new UpdateThingAction()).setId(id).setMeta(meta).setName(th_nm).invoke();
    }

}
