package com.site0.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.Disks;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.WnObjCopying;
import com.site0.walnut.util.ZParams;
import org.nutz.web.WebException;

public class cmd_cpobj extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "lcqnpmvrdQ", "^(mime|quiet)$");
        String ph_src = Wn.normalizeFullPath(params.val_check(0), sys);
        String ph_dst = Wn.normalizeFullPath(params.val_check(1), sys);
        ph_dst = Disks.getCanonicalPath(ph_dst);
        boolean quiet = params.is("quiet");
        boolean asList = params.is("l");
        JsonFormat jfmt = Cmds.gen_json_format(params);

        try {
            // 得到源列表
            List<WnObj> oSrcList = Cmds
                .evalCandidateObjsNoEmpty(sys, Wlang.array(ph_src), 0);

            // 准备 copy 模式
            WnObjCopying woc = new WnObjCopying(sys.io);
            woc.setRecur(params.is("r"));
            woc.setDropBeforeCopy(params.is("d"));
            woc.setCopyMime(params.is("mime"));

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
            List<WnObj> objs = new ArrayList<>(oSrcList.size());
            for (WnObj oSrc : oSrcList) {
                WnObj oDst = woc.exec(oSrc, ph_dst);
                if (null != oDst) {
                    objs.add(oDst);
                }
            }

            if (!quiet) {
                Object out = objs;
                if (!asList && objs.size() == 1) {
                    out = objs.get(0);
                }
                String json = Json.toJson(out, jfmt);
                sys.out.print(json);
            }
        }
        catch (WebException e) {
            if (!params.is("Q")) {
                throw e;
            }
        }
    }

}
