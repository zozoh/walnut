package org.nutz.walnut.ext.hmaker.hdl;

import org.nutz.lang.Files;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.HComFactory;
import org.nutz.walnut.ext.hmaker.util.HmContext;
import org.nutz.walnut.ext.hmaker.util.HPageTranslating;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class hmaker_publish implements JvmHdl {

    private static HComFactory coms = new HComFactory();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 参数太少
        if (hc.params.vals.length < 2) {
            throw Er.create("e.cmd.hmaker.publish.lackArgs");
        }

        // 上下文
        HmContext hpc = new HmContext();

        // 得到源和目标
        hpc.oHome = Wn.checkObj(sys, hc.params.check("home"));
        hpc.oDest = Wn.checkObj(sys, hc.params.check("dest"));

        // 如果限定了源
        WnObj oSrc = hpc.oHome;
        if (hc.params.has("src")) {
            oSrc = Wn.checkObj(sys, hc.params.get("src"));
            // 确保给定的 site 是 src 的父目录
            if (!oSrc.path().startsWith(hpc.oHome.path())) {
                throw Er.create("e.cmd.hmaker.siteNoIncludeSrc", hpc.oHome + " :: " + oSrc);
            }
        }

        // 准备日志输出接口
        Log log = sys.getLog(hc.params);
        Stopwatch sw = Stopwatch.begin();

        // 仅仅处理的是一个文件
        if (oSrc.isFILE()) {
            WnObj re = new HPageTranslating(hpc).translate(oSrc);
            log.info("done : " + re.path());
        }
        // 要处理的是一个目录
        else {
            sys.io.walk(oSrc, (o) -> {
                String mime = o.mime();
                // 必须是文本
                if (null == mime || !mime.startsWith("text/")) {
                    return;
                }

                // 没有扩展名，或者指明是 html 的的文本文件
                String suffixName = Files.getSuffixName(o.path());
                if (Strings.isBlank(suffixName)
                    || suffixName.toLowerCase().matches("^(htm|html|wnml)$")) {

                }
                
                WnObj re = new HPageTranslating(hpc).translate(o);
                log.info("done : " + re.path());
                
            } , WalkMode.LEAF_ONLY);

        }

        // 全部输出完成
        sw.stop();
        log.infof("All done in %dms", sw.getDuration());

    }

}
