package com.site0.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.LoopException;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

/**
 * 基于目录的深度遍历统计, 给出一个树形结构结果。
 * 
 * @author pangw
 *
 */
public class cmd_dircount extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "A");
        boolean showHidden = params.is("A");
        String match = params.getString("match", null);
        String tp = params.getString("tp", null);
        int tarDeep = params.getInt("deep", 1);
        int curDeep = 0;

        String path;
        if (params.vals.length == 0) {
            path = sys.session.getEnv().getString("PWD");
        } else {
            path = params.vals[0];
        }

        WnObj rootObj = sys.io.fetch(null, Wn.normalizeFullPath(path, sys));
        if (rootObj == null) {
            throw Er.create("e.io.obj.noexists", path);
        }
        if (rootObj.isFILE()) {
            throw Er.create("e.io.obj.isFile", path);
        }

        NutMap re = NutMap.NEW();
        re.setv("id", rootObj.id());
        re.setv("name", rootObj.name());
        re.setv("path", rootObj.path());
        // 开始遍历统计
        countChildren(sys, re, rootObj, showHidden, match, tp, curDeep, tarDeep);
        sys.out.print(Json.toJson(re));
    }

    private void countChildren(WnSystem sys,
                               NutMap result,
                               WnObj parentObj,
                               boolean showHidden,
                               String match,
                               String tp,
                               int curDeep,
                               int tarDeep) {
        WnQuery q = Wn.Q.pid(parentObj.id());
        if (!showHidden) {
            q.setv("nm", "^[^.].+$");
        }
        if (!Strings.isBlank(tp)) {
            q.setv("tp", tp);
        }
        if (!Strings.isBlank(match)) {
            q.setAll(Wlang.map(match));
        }
        long childrenNum = sys.io.count(q);
        result.setv("count", childrenNum);
        if (curDeep < tarDeep) {
            List<NutMap> children = new ArrayList<>();
            result.setv("countC", 0);
            result.setv("children", children);
            WnQuery findDir = Wn.Q.pid(parentObj.id());
            findDir.setv("race", "DIR");
            sys.io.each(findDir, new Each<WnObj>() {
                @Override
                public void invoke(int index, WnObj dirObj, int length)
                        throws ExitLoop, ContinueLoop, LoopException {
                    NutMap re = NutMap.NEW();
                    // re.setv("id", dirObj.id());
                    re.setv("name", dirObj.name());
                    children.add(re);
                    countChildren(sys, re, dirObj, showHidden, match, tp, curDeep + 1, tarDeep);
                }
            });
            long ccount = 0;
            for (NutMap child : children) {
                ccount += child.getLong("count");
            }
            result.setv("countC", ccount);
        }
    }
}
