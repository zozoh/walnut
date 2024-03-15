package com.site0.walnut.ext.sys.dirm;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.LoopException;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

/**
 * 
 * 统计指定目录下符合条件的文件数量，并进行mv或cp操作
 * 
 * @author pangwu86
 *
 */
public class cmd_dirm extends JvmExecutor {

    private Log log = Wlog.getCMD();

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        boolean showHidden = params.is("A");
        boolean cp = params.is("cp");
        boolean mv = params.is("mv");
        // 获取指定目录
        WnObj ctar = null;
        if (params.vals.length > 0) {
            ctar = sys.io.fetch(null, Wn.normalizeFullPath(params.val(0), sys));
        } else {
            ctar = sys.getCurrentObj();
        }

        // 检查
        if (!ctar.isDIR()) {
            sys.err.print("dirm need folder");
            return;
        }

        // 计算当年目录有多少合适的数据
        WnQuery q = Wn.Q.pid(ctar.id());
        if (!showHidden) {
            q.setv("nm", "^[^.].+$");
        }
        if (params.has("match")) {
            String json = params.get("match", "{}");
            q.setAll(Wlang.map(json));
        }
        long childrenNum = sys.io.count(q);
        sys.out.println("" + childrenNum);
        log.infof("dirm[%s] count: %d", ctar.id(), childrenNum);
        // 移动操作
        if (cp || mv) {
            String outPath = params.get("out", "~/dirm_" + Wn.now());
            WnObj outDir = sys.io.createIfNoExists(null,
                                                   Wn.normalizeFullPath(outPath, sys),
                                                   WnRace.DIR);
            String outNormalPath = outDir.path();
            sys.out.println("cp/mv to " + outNormalPath);
            sys.io.each(q, new Each<WnObj>() {
                @Override
                public void invoke(int index, WnObj ele, int length)
                        throws ExitLoop, ContinueLoop, LoopException {
                    if (ele.isFILE()) {
                        if (cp) {
                            log.infof("no.%d wnobj[%s] cp to %s", index, ele.id(), outNormalPath);
                            boolean P = params.is("p");
                            sys.exec("cp"
                                     + (P ? " -p" : "")
                                     + " id:"
                                     + ele.id()
                                     + " "
                                     + outNormalPath
                                     + "/");
                        }
                        if (mv) {
                            log.infof("no.%d wnobj[%s] mv to %s", index, ele.id(), outNormalPath);
                            sys.exec("mv" + " id:" + ele.id() + " " + outNormalPath + "/");
                        }
                    } else {
                        log.infof("no.%d wnobj[%s] is dir, cant't cp/mv", index, ele.id());
                    }

                }
            });

        }
    }

}