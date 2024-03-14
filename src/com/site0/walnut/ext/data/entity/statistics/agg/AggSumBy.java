package com.site0.walnut.ext.data.entity.statistics.agg;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.Wn;

public class AggSumBy {

    private int staticValue;
    private boolean morIsNumber;
    private String morValKey;

    public AggSumBy(Object sumBy) {
        staticValue = 1;
        morIsNumber = false;
        morValKey = null;
        if (null != sumBy) {
            if (sumBy instanceof Number) {
                staticValue = ((Number) sumBy).intValue();
            }
            // 直接选择 =mor
            else if ("=mor".equals(sumBy)) {
                morIsNumber = true;
            }
            // 从复杂对象中挑选
            else {
                morValKey = sumBy.toString();
            }
        }
    }

    public int getSum(NutBean his) {
        String mor = his.getString("mor");
        // 根据 Mor 来计算
        if (this.morIsNumber) {
            if (null == mor)
                return 0;
            try {
                return Integer.parseInt(mor);
            }
            catch (NumberFormatException e) {
                return 0;
            }
        }
        // 复杂的 mor
        if (null != morValKey) {
            if (null == mor)
                return 0;
            try {
                NutBean bean = Json.fromJson(NutMap.class, mor);
                Object val = Wn.explainObj(bean, morValKey);
                if (null == val)
                    return 0;
                if (val instanceof Number) {
                    return ((Number) val).intValue();
                }
            }
            catch (Exception e) {
                return 0;
            }
        }
        // 一个静态值
        return this.staticValue;

    }

}
