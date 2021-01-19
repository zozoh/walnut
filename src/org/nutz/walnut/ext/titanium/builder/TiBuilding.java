package org.nutz.walnut.ext.titanium.builder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Nums;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;

import org.nutz.walnut.ext.titanium.builder.action.TiJoinTiJSON;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.builder.action.TiJoinHTML;
import org.nutz.walnut.ext.titanium.builder.action.TiJoinMJS;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class TiBuilding implements Atom {

    private TiBuildEntry entry;

    private WnOutputable out;

    private WnIo io;

    private WnObj oEntry;

    private List<String> outputs;

    private Map<String, TiJoinAction> actions;

    public TiBuilding(WnOutputable out,
                      WnIo io,
                      WnObj oHome,
                      TiBuildEntry entry,
                      List<String> outputs) {
        this.entry = entry;
        this.out = out;
        this.io = io;
        this.oEntry = io.check(oHome, entry.getPath());

        this.outputs = outputs;

        this.actions = new HashMap<String, TiJoinAction>();
        actions.put(".mjs", new TiJoinMJS(entry, outputs));
        actions.put(".html", new TiJoinHTML(entry, outputs));
        actions.put(".json", new TiJoinTiJSON(entry, outputs));
    }

    private void walk(TiBuilderWalker walker, String regex) {
        int[] count = Nums.array(0);
        io.walk(oEntry, f -> {
            if (f.isDIR())
                return;

            if (null != regex) {
                boolean re = f.name().matches(regex);
                if (!re) {
                    out.printlnf("!~ IGNORE: %s", f.path());
                    return;
                }
            }

            String rph = Wn.Io.getRelativePath(oEntry, f);
            if (entry.isSkip(rph)) {
                out.printf("!SKIP: %s\n", rph);
                return;
            }

            // 读一下文件
            String content = io.readText(f);
            int index = count[0]++;
            String[] lines = content.split("\r?\n");
            try {
                walker.run(index, f, rph, lines);
            }
            catch (Exception e) {
                throw Lang.wrapThrow(e);
            }
        }, WalkMode.LEAF_ONLY);
    }

    @Override
    public void run() {
        // 内联模式
        if (oEntry.isFILE() && oEntry.name().matches("^.+[.]m?js$")) {
            this.extendImports();
        }
        // 打包模式
        else {
            this.packToOnFile();
        }
    }

    private void extendImports() {
        // 读一下文件
        String content = io.readText(oEntry);
        String[] lines = content.split("\r?\n");

        String HR = Ws.repeat('#', 50);
        String buildi = Times.format("yyyyMMdd.HHmmss",
                                     Times.D(System.currentTimeMillis()));

        // 逐行扫码，遇到 import 的进行分析
        Matcher m;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 替换版本信息
            if (null != entry.getVersion()) {
                m = entry.getVersion().matcher(line);
                if (m.find()) {
                    String prefix = m.group(1);
                    String version = m.group(2);
                    String suffix = m.group(3);
                    String line2 = String.format("%s%s-%s%s", prefix, version, buildi, suffix);
                    outputs.add(line2);
                    continue;
                }
            }

            // 那么就看看是否需要展开 import 咯
            m = P_I.matcher(line);
            // 导入行 import xxx: 改成赋值模式
            if (m.find()) {
                String varName = Ws.trim(m.group(1));
                String rPath = Ws.trim(m.group(2));

                // 增加一个备注
                outputs.add("//" + HR);
                outputs.add("// # " + line);
                outputs.add("const " + varName + " = (function(){");

                // Import it
                doExtendFiles(this.oEntry, rPath, 0);

                // 结尾
                outputs.add("})();");
            }
            // 普通行，附加
            else {
                outputs.add(line);
            }

        }
    }

    private static Pattern P_I = Pattern.compile("^ *import *([{}\\w, ]+) *from *['\"](.+)['\"] *;? *$");

    private static Pattern P_E = Pattern.compile("^ *export *("
                                                 + "(default|const|class|(async +)?function)? *"
                                                 + "(\\w+) *"
                                                 + "(= *(([\\w ()]+)|([({]+)) *)?"
                                                 + ".*)$");

    private void doExtendFiles(WnObj jsFile, String rPath, int depth) {
        String logPrefix = Ws.repeat("   ", depth);
        String codePrefix = Ws.repeat("  ", depth + 1);
        // ........................................
        // 打开文件
        WnObj file = io.check(jsFile, rPath);
        String content = io.readText(file);
        String[] lines = content.split("\r?\n");
        // ........................................
        out.println(logPrefix + "#" + Ws.repeat('=', 60 - depth * 3));
        out.println(logPrefix + "# " + rPath);
        // ........................................
        // 准备收集 exports 信息
        NutMap exports = new NutMap();
        String defaultExport = null;
        // ........................................
        // 逐行分析
        // ........................................
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 收集 import 信息
            Matcher m = P_I.matcher(line);
            if (m.find()) {
                String varName = Ws.trim(m.group(1));
                String impPath = Ws.trim(m.group(2));
                out.printf(logPrefix + "line:%4d: @import %s from %s\n", i, varName, impPath);

                // 增加一个备注
                outputs.add(codePrefix + "//" + Ws.repeat('#', 50 - codePrefix.length()));
                outputs.add(codePrefix + "// # " + line);
                outputs.add(codePrefix + "const " + varName + " = (function(){");

                // Import it
                this.doExtendFiles(file, impPath, depth + 1);

                // 结尾
                outputs.add(codePrefix + "})();");
            }
            // 收集 export 信息
            else if (line.startsWith("export")) {
                out.printf(logPrefix + " -> line:%4d: %s\n", i, line);
                m = P_E.matcher(line);
                if (!m.find()) {
                    throw Lang.makeThrow("Fail to match export RegExp");
                }
                // out.printf(logPrefix + " -> line:%4d: @export %s\n",
                // i,
                // Dumps.matcherFound(m));
                String exportType = m.group(2);
                String exportName = m.group(4);
                String exportVarN = m.group(7);
                String exportChar = m.group(8);
                String rawLine = m.group(1);
                out.printf(logPrefix + " ----------->: %s\n", rawLine);
                // 默认输出，特别记录一下
                if ("default".equals(exportType)) {
                    defaultExport = exportName;
                }
                // 固定名字输出
                else {
                    exports.put(exportName, exportVarN);
                }

                // 替换原来的行，以便去掉 export
                // 当然，原来就是一个 export const xxx = xxx，那么就不用保留了
                // 最后生成 return 语句的时候，会一并生成的
                if (!exportType.matches("^(const|default)$") || !Strings.isBlank(exportChar)) {
                    outputs.add(codePrefix + rawLine);
                }
            }
            // 普通行，计入
            else {
                outputs.add(codePrefix + line);
            }
        }
        // ........................................
        // 返回部分
        // ........................................
        // 默认输出 export default
        if (!Strings.isBlank(defaultExport)) {
            outputs.add(codePrefix + "return " + defaultExport + ";");
        }
        // 按名称输出 export {...}
        else {
            List<String> ress = new LinkedList<>();
            for (String key : exports.keySet()) {
                String val = exports.getString(key);
                if (null == val) {
                    ress.add(key);
                } else {
                    ress.add(key + ": " + val);
                }
            }
            String reStr = "{" + Strings.join(", ", ress) + "}";
            out.println(logPrefix + "@return " + reStr);
            outputs.add(codePrefix + "return " + reStr + ";");
        }
    }

    private void packToOnFile() {
        String HR = Ws.repeat('=', 60);
        this.walk((index, f, rph, lines) -> {
            out.printf("%3d) %s (%d lines)\n", index, rph, lines.length);
            for (Map.Entry<String, TiJoinAction> en : this.actions.entrySet()) {
                String suffix = en.getKey();
                if (rph.endsWith(suffix)) {
                    outputs.add("//" + HR);
                    outputs.add("// JOIN: " + rph);
                    outputs.add("//" + HR);

                    TiJoinAction ja = en.getValue();

                    String loadUrl = rph;
                    if (!Strings.isBlank(entry.getPrefix())) {
                        loadUrl = entry.getPrefix() + rph;
                    }

                    ja.exec(loadUrl, lines);
                    out.printf("   + => %s\n", ja.getClass().getSimpleName());
                    return;
                }
            }
            out.println("   !!! NilAction !!!");
        }, "^.+\\.(mjs|html|json)$");
    }

    public void logExportDefault() {
        this.walk((index, f, rph, lines) -> {
            out.printf("%3d) %s (%d lines)\n", index, rph, lines.length);
            int i = 0;
            Pattern p = Pattern.compile("export *default *\\{");
            for (; i < lines.length; i++) {
                String line = lines[i];
                if (p.matcher(line).find()) {
                    out.printf("  found 'export default' at line %d", i);
                }
            }
        }, "^.+\\.(mjs|json|html)$");
    }

}
