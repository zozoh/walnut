package org.nutz.walnut.ext.titanium.builder.action;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.builder.TiJoinAction;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;
import org.nutz.walnut.ext.titanium.builder.bean.TiExportItem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class TiJoinMJS extends TiJoinAction {

    public TiJoinMJS(WnIo io,
                     TiBuildEntry entry,
                     List<String> outputs,
                     Map<String, TiExportItem> exportMap,
                     Set<String> depss) {
        super(io, entry, outputs, exportMap, depss);
    }

    private static final Pattern EX_D = Pattern.compile("^\\s*export\\s*default\\s*\\{\\s*$");
    private static final Pattern EX_M = Pattern.compile("^\\s*export\\s*default\\s*([0-9a-zA-Z_]+)\\s*;?$");
    private static final Pattern EX_BY_NAME = Pattern.compile("^\\s*export\\s+(class|function|const|var|let)\\s+([0-9a-zA-Z_]+)\\s*(.*)$");

    private static final Pattern IM_STATIC = Pattern.compile("^\\s*import\\s+(.+?);*$");

    @Override
    public void exec(String url, String[] lines, WnObj f) throws Exception {
        // 找到第一个 export 行，可能是
        // export default {
        // 或者 export default _M;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // 找到 "export default {"
            Matcher m = EX_D.matcher(line);
            if (m.find()) {
                as_export_default(i, url, lines);
                break;
            }
            // 找到 "export default _M;"
            m = EX_M.matcher(line);
            if (m.find()) {
                as_export_module(i, url, lines, m);
                break;
            }
            // 找到 "export const BlotBr = {"
            m = EX_BY_NAME.matcher(line);
            if (m.find()) {
                as_export_by_name(i, url, lines, m);
                break;
            }
            // 找到 import
            m = IM_STATIC.matcher(line);
            if (m.find()) {
                as_import_static(i, url, lines, m, f);
                continue;
            }
        }
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
    }

    private void as_export_by_name(int rowIndex, String rph, String[] lines, Matcher m) {
        String line = lines[rowIndex];
        String varName = m.group(2);

        // 修改当前行
        lines[rowIndex] = line.substring("export".length());

        // 生成代码
        String code = Ws.join(lines, "\n");

        TiExportItem eit = exportMap.get(rph);
        if (null == eit) {
            exportMap.put(rph, new TiExportItem(varName, code));
        } else {
            eit.setName(varName);
            eit.setCode(code);
        }
    }

    private void as_export_default(int rowIndex, String rph, String[] lines) {
        // 前包裹
        outputs.add("(function(){");
        // Copy 之前
        for (int i = 0; i < rowIndex; i++) {
            outputs.add(lines[i]);
        }
        // 替换一下
        outputs.add("const _M = {");
        // Copy 之后
        for (int i = rowIndex + 1; i < lines.length; i++) {
            outputs.add(lines[i]);
        }
        // 写入输出
        outputs.add("Ti.Preload(\"" + rph + "\", _M);");
        // 结束包裹
        outputs.add("})();");
    }

    private void as_export_module(int rowIndex, String rph, String[] lines, Matcher m) {
        // 前包裹
        outputs.add("(function(){");
        // Copy 之前
        for (int i = 0; i < rowIndex; i++) {
            outputs.add(lines[i]);
        }
        // 替换一下
        String varName = m.group(1);
        outputs.add("Ti.Preload(\"" + rph + "\", " + varName + ");");
        // Copy 之后
        for (int i = rowIndex + 1; i < lines.length; i++) {
            outputs.add(lines[i]);
        }
        // 结束包裹
        outputs.add("})();");
    }

}
