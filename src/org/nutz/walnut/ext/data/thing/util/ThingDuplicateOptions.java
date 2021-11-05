package org.nutz.walnut.ext.data.thing.util;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutMap;

public class ThingDuplicateOptions {

    public int dupCount;

    public List<String> toIds;

    public String toKey;

    public String fieldFilter;

    public boolean shallow;

    public NutMap fixedMeta;

    public ThingDuplicateOptions() {
        this.dupCount = 1;
        this.toIds = new LinkedList<>();
        this.toKey = "id";
        this.shallow = false;
    }

    public boolean hasToIds() {
        return null != toIds && toIds.size() > 0;
    }

}
