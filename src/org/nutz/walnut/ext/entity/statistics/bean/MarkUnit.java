package org.nutz.walnut.ext.entity.statistics.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.walnut.api.err.Er;

public abstract class MarkUnit {

    private static Map<String, MarkUnit> instances = new HashMap<>();

    static {
        instances.put("day", new DayMarkUnit());
    }

    public static MarkUnit checkInstance(String type) {
        MarkUnit mu = instances.get(type);
        if (null == mu) {
            throw Er.create("e.statitstics.mark.InvalidType", type);
        }
        return mu;
    }

    public abstract List<MarkRange> evalRanges(long beginInMs, long endInMs);

}
