package org.nutz.walnut.ext.titanium.builder.action;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.ext.titanium.builder.TiJoinAction;
import org.nutz.walnut.ext.titanium.builder.bean.TiBuildEntry;

public class TiJoinMJS extends TiJoinAction {

    public TiJoinMJS(TiBuildEntry entry, List<String> outputs) {
        super(entry, outputs);
    }

    private static final Pattern E_D = Pattern.compile("^ *export *default *\\{ *$");
    private static final Pattern E_M = Pattern.compile("^ *export *default *([0-9a-zA-Z_]+) *;?$");

    @Override
    public void exec(String url, String[] lines) throws Exception {
        // 找到第一个 export 行，可能是
        // export default {
        // 或者 export default _M;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // 找到 "export default {"
            Matcher m = E_D.matcher(line);
            if (m.find()) {
                as_export_default(i, url, lines);
                return;
            }
            // 找到 "export default _M;"
            m = E_M.matcher(line);
            if (m.find()) {
                as_export_module(i, url, lines, m);
                break;
            }
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
