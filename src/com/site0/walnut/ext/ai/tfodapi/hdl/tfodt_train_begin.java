package com.site0.walnut.ext.ai.tfodapi.hdl;

import java.io.File;

import org.nutz.lang.util.Disks;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.ai.tfodapi.cmd_tfodt;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

// 启动或恢复任务
public class tfodt_train_begin implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String id = hc.params.val_check(0);
        WnObj wobj = sys.io.checkById(id);
        File localRoot = new File(Disks.normalize("~/.tfodt/" + wobj.id()));
        Process process = Runtime.getRuntime().exec(new String[] {"./2_开始训练.bat"}, null, localRoot);
        cmd_tfodt.P.put(id, process);
    }

}
