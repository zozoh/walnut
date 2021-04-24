package org.nutz.walnut.ext.ooml.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.ext.ooml.OomlContext;
import org.nutz.walnut.ext.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.ooml.xlsx.XlsxWorkbook;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class ooml_workbook extends OomlFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        String str;
        XlsxWorkbook wb = fc.workbook;
        CheapDocument doc = wb.getDoc();
        String HR = Ws.repeat('-', 60);

        for (int i = 0; i < params.vals.length; i++) {
            String val = params.val(i);
            if (i > 0) {
                sys.out.println(HR);
            }
            // 打印XML源码
            if ("xml".equals(val)) {
                str = doc.toMarkup();
                sys.out.println(str);
            }
            // 打印XML的TREE
            else if ("tree".equals(val)) {
                str = doc.toString();
                sys.out.println(str);
            }
            // 打印资源映射表
            else if ("rels".equals(val)) {
                JsonFormat jfmt = Cmds.gen_json_format(params);
                str = Json.toJson(wb.getRels(), jfmt);
                sys.out.println(str);
            }
            // 打印字符串列表
            else if ("strs".equals(val)) {
                String[] strs = wb.getSharedStrings();
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < strs.length; x++) {
                    sb.append(x).append('.');
                    sb.append(strs[x]);
                    sb.append('\n');
                }
                sys.out.println(sb);
            }
        }
    }

}
