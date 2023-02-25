package org.nutz.walnut.util.explain;

import java.util.Map;

import org.nutz.lang.util.NutBean;

public class WxExplainValMapping implements WnExplain {

    private String key;

    private Map<String, Object> mapping;

    private Object dft;

    public WxExplainValMapping(String val, Map<String, Object> mapping, Object dft) {
        this.key = val;
        this.mapping = mapping;
        this.dft = dft;
    }

    @Override
    public Object explain(NutBean context) {
        Object val = context.get(key);
        if (null != val) {
            Map<String, Object> vMapping = (Map<String, Object>) mapping;
            Object v2 = vMapping.get(val);
            if (null == v2) {
                return dft;
            }
            return v2;
        }
        return val;
    }

}
