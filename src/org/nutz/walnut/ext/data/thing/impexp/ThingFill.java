package org.nutz.walnut.ext.data.thing.impexp;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;

public interface ThingFill {

    /**
     * 将目标导入到thing中，根据业务具体实现导入逻辑
     * 
     * @param sys
     * @param thingSet
     * @param tarObj
     * @return
     */
    boolean doImport(WnSystem sys, WnObj thingSet, WnObj tarObj);

    /**
     * 判断当前obj是否是一个导入的目标对象
     * 
     * @param sys
     * @param curObj
     * @return
     */
    boolean isTarObj(WnSystem sys, WnObj curObj);

}
