package org.nutz.walnut.ext.net.imap.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.net.imap.ImapContext;
import org.nutz.walnut.ext.net.imap.ImapFilter;
import org.nutz.walnut.ext.net.imap.bean.WnEmailMessage;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class imap_tab extends ImapFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "bish");
    }

    @Override
    protected void process(WnSystem sys, ImapContext fc, ZParams params) {

        // 输出摘要
        if (fc.hasSummary()) {
            String json = Json.toJson(fc.summary, JsonFormat.nice());
            sys.out.println(json);
            sys.out.println("##############################");
        }

        // 防守
        if (!fc.hasList()) {
            sys.out.println("- nil messages -");
            return;
        }

        // 输出
        boolean showBorder = params.is("b");
        boolean showHeader = params.is("h");
        boolean showSummary = params.is("s");
        boolean showIndex = params.is("i");
        int indexBase = params.getInt("ibase", 0);

        // 准备列
        String[] cols = Wlang.array("num",
                                    "folder",
                                    "from",
                                    // "to",
                                    // "reply",
                                    "time",
                                    "mime",
                                    "ac",
                                    "subject",
                                    "brief",
                                    "attachment");
        if (params.vals.length > 0) {
            cols = Ws.splitIgnoreBlank(params.val(0));
        }

        if (showIndex) {
            cols = Wlang.arrayFirst("#", cols);
        }

        // 准备输出表
        TextTable tt = new TextTable(cols.length);
        if (showBorder) {
            tt.setShowBorder(true);
        } else {
            tt.setCellSpacing(2);
        }
        // 加标题
        if (showHeader) {
            String[] headers = new String[cols.length];
            for (int i = 0; i < cols.length; i++) {
                headers[i] = Ws.upperFirst(cols[i]);
            }
            tt.addRow(headers);
            tt.addHr();
        }
        // 主体
        int i = indexBase;
        for (WnEmailMessage em : fc.list) {
            List<String> cells = new ArrayList<String>(cols.length);
            for (String key : cols) {
                if ("#".equals(key)) {
                    cells.add("" + (i++));
                    continue;
                }
                Object v = em.get(key);
                cells.add(v == null ? null : v.toString());
            }
            tt.addRow(cells);
        }
        // 尾部
        if (showSummary) {
            tt.addHr();
        }
        // 输出
        sys.out.print(tt.toString());
        if (showSummary) {
            sys.out.printlnf("total %d messages", fc.list.size());
        }
    }

}
