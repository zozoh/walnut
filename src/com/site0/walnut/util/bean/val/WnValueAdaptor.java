package com.site0.walnut.util.bean.val;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.bean.WnValue;

public interface WnValueAdaptor {

    Object toValue(WnValue vd, Object input, NutBean bean);

    // String toStr(WnValue vd, Object val);

}
