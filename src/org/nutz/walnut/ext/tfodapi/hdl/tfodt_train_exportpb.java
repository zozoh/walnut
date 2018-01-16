package org.nutz.walnut.ext.tfodapi.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

// 根据checkpoint生成用于识别的pb文件
public class tfodt_train_exportpb implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String id = hc.params.val(0);
        WnObj wobj = sys.io.checkById(id);
    }

}
