package org.nutz.walnut.ext.entity.statistics.agg;

import java.util.List;

import org.nutz.lang.util.NutBean;

public interface EachAggregation {

    void invoke(String name, List<NutBean> records);

}