package org.nutz.walnut.impl.box.cmd;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_cp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "pvr");
        if (params.vals.length != 2) {
            throw Err.create("e.cmds.cp.not_enugh_args");
        }
        String ph_src = Wn.normalizeFullPath(params.vals[0], sys);
        String ph_dst = Wn.normalizeFullPath(params.vals[1], sys);
        ph_dst = Disks.getCanonicalPath(ph_dst);

        // 得到源
        List<WnObj> oSrcList = Cmds.evalCandidateObjsNoEmpty(sys, Lang.array(ph_src), 0);

        // 准备 copy 模式
        int mode = 0;
        if (params.is("r"))
            mode |= Wn.Io.RECUR;
        if (params.is("p"))
            mode |= Wn.Io.PROP;
        if (params.is("v"))
            mode |= Wn.Io.VERBOSE;

        // 执行 copy
        for (WnObj oSrc : oSrcList)
            Wn.Io.copy(sys, mode, oSrc, ph_dst);
    }

}
