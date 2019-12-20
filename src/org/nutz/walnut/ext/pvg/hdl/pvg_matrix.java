package org.nutz.walnut.ext.pvg.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.ext.pvg.BizPvgService;
import org.nutz.walnut.ext.pvg.cmd_pvg;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(json)$")
public class pvg_matrix implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到服务类
        BizPvgService pvgs = cmd_pvg.getPvgService(hc);
        boolean JSON = hc.params.is("json");
        String tmpl = hc.params.get("t");

        // 得到输出模板
        PvgMatrixCell pmc = new PvgMatrixCell(tmpl, JSON);
        NutMap matrix = pvgs.getMatrix();

        // JSON 模式输出
        if (JSON) {
            for (String role : matrix.keySet()) {
                NutMap actions = matrix.getAs(role, NutMap.class);
                for (Map.Entry<String, Object> en : actions.entrySet()) {
                    Object val = pmc.toJson(en.getValue());
                    en.setValue(val);
                }
            }
            sys.out.println(Json.toJson(matrix, hc.jfmt));
        }
        // 默认为表格模式数据
        else {
            List<String> anms = pvgs.getActionNames();
            int len = anms.size() + 1;
            List<String> cols = new ArrayList<>(len);
            if (hc.params.has("ibase")) {
                cols.add("#");
            }
            cols.add("Role");
            cols.addAll(anms);
            // 准备输出表
            TextTable tt = new TextTable(cols.size());
            tt.setShowBorder(true);
            tt.setCellSpacing(1);
            // 加标题
            tt.addRow(cols);
            tt.addHr();

            // 主体
            int i = hc.params.getInt("ibase", 0);
            for (String role : pvgs.getMatrix().keySet()) {
                NutMap map = matrix.getAs(role, NutMap.class);
                if (null == map) {
                    continue;
                }
                List<String> cells = new ArrayList<String>(len);
                cells.add(role);
                for (String key : anms) {
                    if ("#".equals(key)) {
                        cells.add("" + (i++));
                        continue;
                    }
                    Object v = Mapl.cell(map, key);
                    String s = pmc.toCell(v);
                    cells.add(s);
                }
                tt.addRow(cells);
            }
            // 尾部
            tt.addHr();

            // 输出结果
            sys.out.println(tt.toString());

        }
    }

    static class PvgMatrixCell {

        Object yes;
        Object no;

        PvgMatrixCell(String tmpl, boolean JSON) {
            String[] ss = null;
            if (!Strings.isBlank(tmpl)) {
                ss = tmpl.trim().split(":");
            }
            // 默认设置
            if (null == ss || ss.length == 0) {
                // JSON 模式
                if (JSON) {
                    yes = true;
                    no = false;
                }
                // 默认为表格模式
                else {
                    yes = "Yes";
                    no = "--";
                }
            }
            // 读取模板定义
            else {
                // "Yes:N/A"
                // "On:Off"
                // "true:false"
                // ":No"
                // ":false"
                if (ss.length >= 2) {
                    yes = eval(ss[0]);
                    no = eval(ss[1]);
                }
                // "Yes"
                // "true"
                else {
                    yes = eval(ss[0]);
                    no = null;
                }
            }
        }

        Object toJson(Object input) {
            if (Castors.me().castTo(input, boolean.class)) {
                return yes;
            }
            return no;
        }

        String toCell(Object input) {
            if (Castors.me().castTo(input, boolean.class)) {
                return toDisplayStr(yes);
            }
            return toDisplayStr(no);
        }

        String toDisplayStr(Object input) {
            if (null == input) {
                return "";
            }
            if (input instanceof Boolean) {
                if ((Boolean) input) {
                    return "true";
                }
                return "false";
            }
            return input.toString();
        }

        Object eval(String s) {
            String trim = Strings.trim(s);
            if (null == trim || trim.length() == 0) {
                return null;
            }
            if ("true".equals(trim)) {
                return true;
            }
            if ("false".equals(trim)) {
                return false;
            }
            return trim;
        }
    }

}
