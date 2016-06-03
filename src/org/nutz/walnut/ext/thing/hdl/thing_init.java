package org.nutz.walnut.ext.thing.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class thing_init implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 找到集合
        WnObj oTS = Things.checkThingSet(hc.oHome);

        // 执行初始化命令
        sys.exec("cat id:" + oTS.id() + "/thing_init | app-init id:" + oTS.id());

    }

}
