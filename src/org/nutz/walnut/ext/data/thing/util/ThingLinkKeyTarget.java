package org.nutz.walnut.ext.data.thing.util;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class ThingLinkKeyTarget {

    private String id;

    private String thingSet;

    private NutMap filter;

    public ThingLinkKeyTarget() {}

    public ThingLinkKeyTarget(Map<String, Object> map) {
        NutMap bean = NutMap.WRAP(map);
        this.id = bean.getString("id");
        this.thingSet = bean.getString("thingSet");
        this.filter = bean.getAs("filter", NutMap.class);
    }

    public ThingLinkKeyTarget clone() {
        ThingLinkKeyTarget re = new ThingLinkKeyTarget();
        re.id = this.id;
        re.thingSet = this.thingSet;
        if (null != this.filter) {
            re.filter = this.filter.duplicate();
        }
        return re;
    }

    public void explain(NutBean vars) {
        this.id = (String) Wn.explainObj(vars, this.id);
        this.thingSet = (String) Wn.explainObj(vars, this.thingSet);
        this.filter = (NutMap) Wn.explainObj(vars, this.filter);
    }

    public boolean isAva() {
        return !this.hasId() || !this.hasFilter();
    }

    public boolean hasId() {
        return !Ws.isBlank(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasThingSet() {
        return !Ws.isBlank(thingSet);
    }

    public String getThingSet() {
        return thingSet;
    }

    public void setThingSet(String thingSet) {
        this.thingSet = thingSet;
    }

    public boolean hasFilter() {
        return null != filter && filter.size() > 0;
    }

    public NutMap getFilter() {
        return filter;
    }

    public void setFilter(NutMap filter) {
        this.filter = filter;
    }

}
