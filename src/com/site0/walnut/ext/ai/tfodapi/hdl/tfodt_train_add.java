package com.site0.walnut.ext.ai.tfodapi.hdl;

import java.io.File;

import org.nutz.lang.Files;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

// 新建一个训练任务
public class tfodt_train_add implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnObj wobj = sys.io.createIfNoExists(null, Wn.normalizeFullPath("~/.tfodt/", sys) + "${id}", WnRace.DIR);
        File localRoot = Files.createDirIfNoExists(Wn.normalizeFullPath("~/.tfodt/" + wobj.id(), sys));
        new File(localRoot, "data/").mkdir();
        new File(localRoot, "init/").mkdir();
        new File(localRoot, "model/").mkdir();
        // TODO 拷贝bat/pbtxt/model config文件
    }

}
