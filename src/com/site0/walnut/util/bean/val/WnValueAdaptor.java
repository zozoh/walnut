package com.site0.walnut.util.bean.val;

import com.site0.walnut.util.bean.WnValue;

public interface WnValueAdaptor {

    Object toValue(WnValue vd, Object input);

    //String toStr(WnValue vd, Object val);

}
