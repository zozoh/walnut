package org.nutz.walnut.ext.tfodapi.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

// 新建一个训练任务
public class tfodt_train_stop implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        sys.out.print("nop yet");
    }

}
