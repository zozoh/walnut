package org.nutz.walnut.impl.box.cmd;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wlang;
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

        // 固定参数
        NutMap meta = null;
        if (params.has("meta")) {
            String json = Cmds.checkParamOrPipe(sys, params, "meta", true);
            meta = Wlang.map(json);
        }

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
        NutMap fixedMeta = meta;
        for (WnObj oSrc : oSrcList) {
            Wn.Io.copy(sys, mode, oSrc, ph_dst, (o) -> {
                if (null != fixedMeta) {
                    sys.io.appendMeta(o, fixedMeta);
                }
            });
        }
    }

}
