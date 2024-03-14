package com.site0.walnut.ext.ai.tfodapi.hdl;

import java.io.File;

import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

// 根据素材生成tfrecord文件
public class tfodt_tfrecord_create implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String id = hc.params.val_check(0);
        WnObj wobj = sys.io.checkById(id);
        File localRoot = new File(Disks.normalize("~/.tfodt/" + wobj.id()));
        Process process = Runtime.getRuntime().exec(new String[] {"./1_生成数据集.bat"}, null, localRoot);
        int re = process.waitFor();
        sys.out.writeJson(new NutMap("exit", re));
    }

}
