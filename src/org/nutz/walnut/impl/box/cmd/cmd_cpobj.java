package org.nutz.walnut.impl.box.cmd;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnObjCopying;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.WebException;

public class cmd_cpobj extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "pmvrdQ");
        String ph_src = Wn.normalizeFullPath(params.val_check(0), sys);
        String ph_dst = Wn.normalizeFullPath(params.val_check(1), sys);
        ph_dst = Disks.getCanonicalPath(ph_dst);

        try {
            // 得到源列表
            List<WnObj> oSrcList = Cmds.evalCandidateObjsNoEmpty(sys, Lang.array(ph_src), 0);

            // 准备 copy 模式
            WnObjCopying woc = new WnObjCopying(sys.io);
            woc.setRecur(params.is("r"));
            woc.setDropBeforeCopy(params.is("d"));

            // 准备所属用户/组
            woc.setOwn(params.get("own"));
            woc.setGrp(params.get("grp"));

            // 元数据·复制标准属性以外所有元数据
            if (params.is("p")) {
                woc.setPropDefaultFilter();
            }
            // 元数据·复制标准属性以外所有元数据以及这四个标准属性 `c|m|g|md`
            else if (params.is("m")) {
                woc.setPropOwnerFilter();
            }
            // 元数据·正则表达式指定copy特殊的元数据，支持 ! 语法
            else if (params.has("e")) {
                woc.setPropFilter(params.get("e"));
            }

            // 显示详情模式
            if (params.is("v")) {
                WnObj oCurrent = sys.getCurrentObj();
                woc.setCallback((oTa, oDst) -> {
                    String rph = Wn.Io.getRelativePath(oCurrent, oTa);
                    sys.out.println(rph);
                });
            }

            // 执行 copy
            for (WnObj oSrc : oSrcList)
                woc.exec(oSrc, ph_dst);
        }
        catch (WebException e) {
            if (!params.is("Q")) {
                throw e;
            }
        }
    }

}
