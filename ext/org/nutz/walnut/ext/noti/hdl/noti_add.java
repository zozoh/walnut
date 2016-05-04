package org.nutz.walnut.ext.noti.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class noti_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        
        // 查看消息雷系
        String notiType = hc.params.val_check(0);
        
    }

}
