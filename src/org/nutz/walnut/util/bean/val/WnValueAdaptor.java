package org.nutz.walnut.util.bean.val;

import org.nutz.walnut.util.bean.WnValue;

public interface WnValueAdaptor {

    Object toValue(WnValue vd, Object input);

    //String toStr(WnValue vd, Object val);

}
