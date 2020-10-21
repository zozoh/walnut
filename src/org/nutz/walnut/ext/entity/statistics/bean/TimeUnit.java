package org.nutz.walnut.ext.entity.statistics.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.walnut.api.err.Er;

public abstract class TimeUnit {

    private static Map<String, TimeUnit> instances = new HashMap<>();

    static {
        instances.put("day", new DayTimeUnit());
    }

    public static TimeUnit checkInstance(String type) {
        TimeUnit mu = instances.get(type);
        if (null == mu) {
            throw Er.create("e.statitstics.mark.InvalidType", type);
        }
        return mu;
    }

    public abstract List<NamedAmsRange> splitRanges(AmsRange range);

    public abstract AmsRange padRange(long beginInMs, long endInMs);

}
