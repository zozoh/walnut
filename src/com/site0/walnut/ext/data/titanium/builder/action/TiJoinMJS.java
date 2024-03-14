package com.site0.walnut.ext.data.titanium.builder.action;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.titanium.builder.TiJoinAction;
import com.site0.walnut.ext.data.titanium.builder.bean.TiBuildEntry;
import com.site0.walnut.ext.data.titanium.builder.bean.TiExportItem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class TiJoinMJS extends TiJoinAction {

    public TiJoinMJS(WnIo io,
                     TiBuildEntry entry,
                     List<String> outputs,
                     Map<String, TiExportItem> exportMap,
                     Set<String> depss,
                     Map<String, Integer> importCount) {
        super(io, entry, outputs, exportMap, depss, importCount);
    }

    // private static final Pattern EX_D =
    // Pattern.compile("^\\s*export\\s*default\\s*\\{\\s*$");
    // private static final Pattern EX_M =
    // Pattern.compile("^\\s*export\\s*default\\s*([0-9a-zA-Z_]+)\\s*;?$");
    // private static final Pattern EX_BY_NAME =
    // Pattern.compile("^\\s*export\\s+(class|function|const|var|let)\\s+([0-9a-zA-Z_]+)\\s*(.*)$");
    private static final Pattern EX_BY_ANY = Pattern.compile("^\\s*export\\s+(default)?\\s*(.+?)\\s*$");

    private static final Pattern IM_STATIC = Pattern.compile("^\\s*import\\s+(.+?);*$");

    @Override
    public void exec(String url, String[] lines, WnObj f) throws Exception {
        // 找到第一个 export 行，可能是
        // export default {
        // 或者 export default _M;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // 找到 "export default {"
            Matcher m = EX_BY_ANY.matcher(line);
            if (m.find()) {
                as_export(i, url, lines, m);
                // 找到了，就直接输出，不用继续找了
                break;
            }
            // // 找到 "export default _M;"
            // m = EX_M.matcher(line);
            // if (m.find()) {
            // as_export_module(i, url, lines, m);
            // break;
            // }
            // // 找到 "export const BlotBr = {"
            // m = EX_BY_NAME.matcher(line);
            // if (m.find()) {
            // as_export_by_name(i, url, lines, m);
            // break;
            // }
            // // 找到 "export {xxx}"
            // m = EX_BY_ANY.matcher(line);
            // if (m.find()) {
            // as_export_by_any(i, url, lines, m);
            // break;
            // }
            // 找到 import
            m = IM_STATIC.matcher(line);
            if (m.find()) {
                as_import_static(i, url, lines, m, f);
                continue;
            }
        }
    }

    private void as_export(int rowIndex, String rph, String[] lines, Matcher m) {
        boolean isDefault = !Ws.isBlank(m.group(1));
        String exportName = m.group(2);
        String varName = null;
        String code;

        // 如果是变量名导出的话，那么最后追加一个 return
        if (exportName.matches("^([\\w_][\\d\\w_]*)$")) {
            lines[rowIndex] = "";
            code = Ws.join(lines, "\n");
            code += "return " + exportName + ";\n";
            // 不是默认导出的话，就是一个 var 咯
            if (!isDefault) {
                varName = exportName;
            }
        }
        // 最后一行的话，直接就 return了
        else if (rowIndex == (lines.length - 1)) {
            lines[rowIndex] = "return " + exportName;
            code = Ws.join(lines, "\n");
        }
        // 否则，搞一个内部变量
        else {
            lines[rowIndex] = "const __TI_MOD_EXPORT_VAR_NM = " + exportName;
            code = Ws.join(lines, "\n");
            code += "\nreturn __TI_MOD_EXPORT_VAR_NM;";
        }

        // 加入输出
        __join_export_and_output(rph, varName, code);
    }

    private void as_import_static(int rowIndex, String rph, String[] lines, Matcher m, WnObj f) {
        String str = Ws.trim(m.group(1));

        // 分析一下 xxx from 'xxx'
        String[] ss = str.split("from");

        if (ss.length != 2) {
            throw Lang.impossible();
        }

        // 引入什么呢？
        String varName = Ws.trim(ss[0]);

        // 从那里引入呢?
        String fromPath = Ws.trim(ss[1]);
        if (Ws.isQuoteBy(fromPath, '"', '"') || Ws.isQuoteBy(fromPath, '\'', '\'')) {
            fromPath = fromPath.substring(1, fromPath.length() - 1);
        } else {
            throw Lang.impossible();
        }

        // 找到一个路径
        String parentPath = Files.getParent(rph);
        if (fromPath.startsWith("./")) {
            fromPath = fromPath.substring(2);
        }
        String thePath = Wn.appendPath(parentPath, fromPath);

        // 记入引入表
        TiExportItem eit = exportMap.get(rph);
        if (null == eit) {
            WnObj o = io.check(f, fromPath);
            this.exportMap.put(thePath, new TiExportItem(varName, o));
        } else if (!eit.hasCode()) {
            WnObj o = io.check(f, fromPath);
            eit.setObj(o);
        }

        // 生成代码
        String code = String.format("const %s = window.TI_PACK_EXPORTS['%s'];", varName, thePath);
        lines[rowIndex] = code;
        Integer c = importCount.get(thePath);
        if (null == c) {
            importCount.put(thePath, 1);
        } else {
            importCount.put(thePath, c + 1);
        }
    }

    private void __join_export_and_output(String rph, String varName, String code) {
        // 这个是准备导出的
        TiExportItem eit = exportMap.get(rph);
        if (null == eit) {
            exportMap.put(rph, new TiExportItem(null, code));
        } else {
            eit.setName(varName);
            eit.setCode(code);
        }

        // 加入 Output，准备应对 Proload
        outputs.add("Ti.Preload(\"" + rph + "\", TI_PACK_EXPORTS['" + rph + "']);");
    }
}
