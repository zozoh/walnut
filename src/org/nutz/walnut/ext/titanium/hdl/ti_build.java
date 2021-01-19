package org.nutz.walnut.ext.titanium.hdl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.titanium.builder.TiBuilding;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildConfig;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildTarget;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class ti_build implements JvmHdl {

    private static final String HR = Ws.repeat('-', 40);
    private static final String sepLine = Ws.repeat("//", 30);

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析参数，找到 ti-build.json
        String confPath = hc.params.val(0, "ti-build.json");
        String aConfPath = Wn.normalizeFullPath(confPath, sys);
        WnObj oConf = sys.io.check(null, aConfPath);
        WnObj oHome;

        // 如果是个目录
        if (oConf.isDIR()) {
            oHome = oConf;
            oConf = sys.io.check(oHome, "ti-build.json");
        }
        // 否则就必须是文件咯
        else {
            oHome = oConf.parent();
        }
        //
        // 分析配置文件
        //
        TiBuildConfig conf = sys.io.readJson(oConf, TiBuildConfig.class);

        // 准备时间戳
        Date now = Times.now();
        String packTime = Times.format("yyyy-MM-dd HH:mm:ss", now);

        // Prepare output targets
        Map<String, List<String>> targetOutputs = new HashMap<>();
        for (String targetName : conf.getTargets().keySet()) {
            targetOutputs.put(targetName, new LinkedList<>());
        }
        //
        // Building
        //
        sys.out.println(HR);
        sys.out.printf("BUIDING: %d entry\n", conf.getEntries().length);
        for (TiBuildEntry et : conf.getEntries()) {
            List<String> outputs = targetOutputs.get(et.getTarget());
            TiBuilding ing = new TiBuilding(sys.out, sys.io, oHome, et, outputs);
            ing.run();
        }
        //
        // Ouput
        sys.out.println(HR);
        sys.out.printf("OUTPUT: %d targets\n", conf.getTargets().size());
        for (String targetName : conf.getTargets().keySet()) {
            TiBuildTarget tar = conf.getTargets().get(targetName);
            String targetPath = tar.getPath();

            // 准备输出
            List<String> outputs = targetOutputs.get(targetName);

            // 无需输出
            if (outputs.isEmpty()) {
                sys.out.println("  ~ nil ouput ~");
                continue;
            }

            // 需要包裹一下
            if (tar.isWrap()) {
                // 前包裹
                outputs.add(0, "(function(){");
                // 结束包裹
                outputs.add(sepLine);
                outputs.add("// The End");
                outputs.add("})();");
            }

            // 再加一个打包日期
            outputs.add(0, "// Pack At: " + packTime);

            // 准备内容并写入
            String content = Strings.join(System.lineSeparator(), outputs);

            WnObj oTa = sys.io.createIfNoExists(oHome, targetPath, WnRace.FILE);
            sys.io.writeText(oTa, content);
            sys.out.printf("  + %s\n", targetPath);
        }
    }

}
