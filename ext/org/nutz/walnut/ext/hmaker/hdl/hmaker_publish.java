package org.nutz.walnut.ext.hmaker.hdl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class hmaker_publish implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        if (hc.params.vals.length < 2) {
            throw Er.create("e.cmd.hmaker.publish.lackArgs");
        }

        // 得到源和目标
        WnObj oSrc = Wn.checkObj(sys, hc.params.vals[0]);
        String aph = Wn.normalizeFullPath(hc.params.vals[1], sys);
        // WnObj oDst = sys.io.createIfNoExists(p, path, race)
        WnObj oDst = Wn.checkObj(sys, hc.params.vals[1]);

        // 仅仅处理的是一个文件
        if (oSrc.isFILE()) {

        }
        // 要处理的是一个目录
        else {
            // 那么目标也一定是个目录
            if (oDst.isFILE())
                oDst = oDst.parent();

        }

    }

}
