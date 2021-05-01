package org.nutz.walnut.ext.media.ooml;

import java.io.IOException;
import java.io.InputStream;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.ooml.OomlPackage;
import org.nutz.walnut.util.Wn;

public class cmd_ooml extends JvmFilterExecutor<OomlContext, OomlFilter> {

    public cmd_ooml() {
        super(OomlContext.class, OomlFilter.class);
    }

    @Override
    protected OomlContext newContext() {
        return new OomlContext();
    }

    @Override
    protected void prepare(WnSystem sys, OomlContext fc) {
        InputStream ins;
        // 从标准输入读取
        if (fc.params.vals.length == 0) {
            ins = sys.in.getInputStream();
        }
        // 从路径读取
        else {
            WnObj oInput = Wn.checkObj(sys, fc.params.val_check(0));
            ins = sys.io.getInputStream(oInput, 0);
        }

        // 读取压缩包全部的内容
        try {
            fc.ooml = new OomlPackage();
            fc.ooml.loadEntriesAndClose(ins);
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }

    }

    @Override
    protected void output(WnSystem sys, OomlContext fc) {}

}
