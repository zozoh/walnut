package com.site0.walnut.ext.data.titanium.builder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Nums;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.api.io.WalkMode;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.titanium.builder.action.TiJoinHTML;
import com.site0.walnut.ext.data.titanium.builder.action.TiJoinMJS;
import com.site0.walnut.ext.data.titanium.builder.action.TiJoinTiJSON;
import com.site0.walnut.ext.data.titanium.builder.bean.TiBuildEntry;
import com.site0.walnut.ext.data.titanium.builder.bean.TiExportItem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class TiBuilding implements Atom {

    private TiBuildEntry entry;

    private WnOutputable out;

    private WnIo io;

    private WnObj oEntry;

    private List<String> outputs;

    private Set<String> depss;

    private Map<String, TiJoinAction> actions;

    public TiBuilding() {}

    public TiBuilding(WnOutputable out,
                      WnIo io,
                      WnObj oHome,
                      TiBuildEntry entry,
                      List<String> outputs,
                      Map<String, TiExportItem> exportMap,
                      Set<String> depss,
                      Map<String, Integer> importCount) {
        this.entry = entry;
        this.out = out;
        this.io = io;
        this.oEntry = io.check(oHome, entry.getPath());

        this.outputs = outputs;
        this.depss = depss;

        this.actions = new HashMap<String, TiJoinAction>();
        actions.put(".mjs", new TiJoinMJS(io, entry, outputs, exportMap, depss, importCount));
        actions.put(".html", new TiJoinHTML(io, entry, outputs, exportMap, depss, importCount));
        actions.put(".json", new TiJoinTiJSON(io, entry, outputs, exportMap, depss, importCount));
    }

    public TiBuilding clone() {
        TiBuilding ing = new TiBuilding();
        ing.entry = this.entry;
        ing.out = this.out;
        ing.io = this.io;
        ing.oEntry = this.oEntry;
        ing.outputs = new LinkedList<>();
        ing.actions = this.actions;
        return ing;
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

    static final String HR2 = Ws.repeat('=', 40);
    static final String HR3 = Ws.repeat('#', 50);

    private void extendImports() {
        // 读一下文件
        String content = io.readText(oEntry);
        String[] lines = content.split("\r?\n");

        String buildi = Times.format("yyyyMMdd.HHmmss", Times.D(System.currentTimeMillis()));

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
                outputs.add("//" + HR3);
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

    public TiJoinAction getAction(String suffix) {
        return actions.get(suffix);
    }

    private void packToOnFile() {
        this.walk((index, f, rph, lines) -> {
            out.printf("%3d) %s (%d lines)\n", index, rph, lines.length);
            String loadUrl = rph;
            if (!Strings.isBlank(entry.getPrefix())) {
                loadUrl = entry.getPrefix() + rph;
            }
            execAction(loadUrl, lines, f);
        }, "^.+\\.(mjs|html|json)$");
    }

    public void execAction(String loadUrl, String[] lines, WnObj f) throws Exception {
        String suffix = Files.getSuffix(loadUrl);
        TiJoinAction ja = this.getAction(suffix);
        outputs.add("//" + HR2);
        outputs.add(String.format("// JOIN <%s> %s", f.name(), loadUrl));
        outputs.add("//" + HR2);
        if (null != ja) {
            ja.exec(loadUrl, lines, f);
            out.printf("   + => %s\n", ja.getClass().getSimpleName());
        } else {
            out.println("   !!! NilAction !!!");
        }
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

    public List<String> getOutputs() {
        return outputs;
    }

    public boolean hasOutputs() {
        return null != outputs && !outputs.isEmpty();
    }

    public Set<String> getDepss() {
        return depss;
    }

    public boolean hasDepss() {
        return null != depss && !depss.isEmpty();
    }

}
