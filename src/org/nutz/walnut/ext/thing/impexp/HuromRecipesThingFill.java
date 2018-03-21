package org.nutz.walnut.ext.thing.impexp;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 没地方放，暂时放一下，稍后删掉
 * 
 * @author pw
 *
 */
public class HuromRecipesThingFill implements ThingFill {

    @Override
    public boolean doImport(WnSystem sys, WnObj thingSet, WnObj tarObj) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTarObj(WnSystem sys, WnObj curObj) {
        // 目录，下面有 01.jpg 和 xxx.txt
        if (curObj.isDIR()) {
            if (sys.io.exists(curObj, "01.jpg")) {
                if (sys.io.count(Wn.Q.pid(curObj.id()).setv("tp", "txt")) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

}
