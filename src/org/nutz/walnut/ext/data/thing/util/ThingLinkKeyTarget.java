package org.nutz.walnut.ext.data.thing.util;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class ThingLinkKeyTarget {

    private String thingSet;

    private NutMap filter;

    public boolean hasThingSet() {
        return !Strings.isBlank(thingSet);
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
