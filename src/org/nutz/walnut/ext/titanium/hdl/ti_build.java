package org.nutz.walnut.ext.titanium.hdl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.titanium.builder.TiBuilding;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildConfig;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildTarget;
import org.nutz.walnut.ext.titanium.builder.bean.TiExportItem;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wcol;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class ti_build implements JvmHdl {

    private static final String HR = Ws.repeat('-', 60);
    private static final String HR2 = Ws.repeat('=', 60);
    private static final String sepLine = Ws.repeat("/", 50);

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
        Map<String, Map<String, TiExportItem>> targetExports = new HashMap<>();
        Map<String, Set<String>> depsOutput = new HashMap<>();
        Map<String, Map<String, Integer>> importCounts = new HashMap<>();

        for (String targetName : conf.getTargets().keySet()) {
            targetOutputs.put(targetName, new LinkedList<>());
            targetExports.put(targetName, new HashMap<>());
            depsOutput.put(targetName, new HashSet<>());
            importCounts.put(targetName, new HashMap<>());
        }

        //
        // Building
        //
        sys.out.println(HR);
        sys.out.printf("BUIDING: %d entry\n", conf.getEntries().length);
        for (TiBuildEntry et : conf.getEntries()) {
            String taName = et.getTarget();
            List<String> outputs = targetOutputs.get(taName);
            Set<String> depss = depsOutput.get(taName);
            Map<String, TiExportItem> exports = targetExports.get(taName);
            Map<String, Integer> importCount = importCounts.get(taName);

            TiBuilding ing = new TiBuilding(sys.out,
                                            sys.io,
                                            oHome,
                                            et,
                                            outputs,
                                            exports,
                                            depss,
                                            importCount);
            ing.run();

            // 检查所有的导出列表，如果有 null的，表示需要导入 mjs
            for (Map.Entry<String, TiExportItem> en : exports.entrySet()) {
                String rph = en.getKey();
                TiExportItem val = en.getValue();
                if (!val.hasCode() && val.hasObj()) {
                    WnObj oI = val.getObj();
                    String content = sys.io.readText(oI);
                    String[] iLines = content.split("\r?\n");
                    TiBuilding ing2 = ing.clone();
                    ing2.execAction(rph, iLines, oI);
                    if (!val.hasCode() && ing2.hasOutputs()) {
                        String code = Wcol.join(ing2.getOutputs(), "\n");
                        val.setCode(code);
                    }
                }
            }
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

            // 准备前段和后端
            List<String> heads = new LinkedList<>();
            List<String> tails = new LinkedList<>();

            // 准备一个导出的文件集合
            Map<String, TiExportItem> exports = targetExports.get(targetName);
            Map<String, Integer> importCount = importCounts.get(targetName);
            if (!exports.isEmpty()) {
                heads.add("// " + HR2);
                heads.add("// OUTPUT TARGET IMPORTS");
                heads.add("// " + HR2);
                heads.add("(function(){");
                heads.add("window.TI_PACK_EXPORTS = {};");

                // 所有优先导入的项
                heads.add("// " + HR2);
                heads.add("// LV1 : imports");
                heads.add("// " + HR2);
                List<String> lv1Keys = new ArrayList<>(exports.size());
                for (Map.Entry<String, TiExportItem> en : exports.entrySet()) {
                    String key = en.getKey();
                    Integer c = importCount.get(key);
                    if (null != c && c > 0) {
                        lv1Keys.add(key);
                        joinHeadImport(heads, en);
                    }
                }

                // 移除优先导入项
                for (String key : lv1Keys) {
                    exports.remove(key);
                }

                heads.add("// " + HR2);
                heads.add("// LV2 : imports - (" + lv1Keys.size() + ")");
                heads.add("// " + HR2);
                // 剩下的 import
                for (Map.Entry<String, TiExportItem> en : exports.entrySet()) {
                    joinHeadImport(heads, en);
                }

                heads.add("})();   // ~ windows.TI_EXPORTS");
            }

            // 需要包裹一下
            if (tar.isWrap()) {
                // 前包裹
                heads.add("(function(){");
                // 结束包裹
                tails.add(sepLine);
                tails.add("// The End");
                tails.add("})();");
            }

            // 插入输出内容
            outputs.addAll(0, heads);
            outputs.addAll(tails);

            // 如果需要加载额外的库，输出为一个列表文档
            Set<String> depss = depsOutput.get(targetName);
            if (!depss.isEmpty()) {
                String depsPath = tar.getDepsPath();
                WnObj oDeps = sys.io.createIfNoExists(oHome, depsPath, WnRace.FILE);
                List<NutMap> depsList = new ArrayList<>(depss.size());
                for (String deps : depss) {
                    String type = Files.getSuffixName(deps);
                    NutMap bean = Lang.map("type", type);
                    bean.put("path", deps);
                    depsList.add(bean);
                }
                String json = Json.toJson(depsList, JsonFormat.nice());
                sys.io.writeText(oDeps, json);
                // String loads = Json.toJson(depss, JsonFormat.nice());
                // outputs.add(0, String.format("await Ti.Load(%s);", loads));
                // outputs.add(0, "////////////async loading////////////////");
                // outputs.add(0, "(async function(){");
                // outputs.add("////////////async loading////////////////");
                // outputs.add("})()");
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

    private void joinHeadImport(List<String> heads, Map.Entry<String, TiExportItem> en) {
        String key = en.getKey();
        String fnm = Files.getName(key);
        TiExportItem val = en.getValue();
        heads.add("// " + HR2);
        String valName = val.getName();
        heads.add(String.format("// EXPORT '%s' -> %s", fnm, valName));
        heads.add("// " + HR2);
        String str;
        // 集合导出
        if (null == valName || Ws.isQuoteBy(valName, '{', '}')) {
            str = String.format("window.TI_PACK_EXPORTS['%s'] = (function(){\n" + "%s;\n" + "})()",
                                key,
                                val.getCode());
        }
        // 单变量导出
        else {
            str = String.format("window.TI_PACK_EXPORTS['%s'] = {\n"
                                + "%s : (function(){\n"
                                + "%s;\n"
                                + "return %2$s;"
                                + "})()}",
                                key,
                                valName,
                                val.getCode());
        }
        // 记入 ...
        heads.add(str);
    }

}
