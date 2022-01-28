package org.nutz.walnut.ext.data.o.util;

import java.util.List;

public interface WnPop {

    <T extends Object> List<T> exec(List<T> list);

}
