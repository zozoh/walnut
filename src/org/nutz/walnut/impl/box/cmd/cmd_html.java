package org.nutz.walnut.impl.box.cmd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_html extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        // 读取内容
        String ph = params.val(0);
        String html;
        if (!Strings.isBlank(ph)) {
            WnObj oHtml = Wn.checkObj(sys, ph);
            html = sys.io.readText(oHtml);
        }
        // 从标准输入读取
        else {
            html = sys.in.readAll();
        }

        // 解析
        Document doc = Jsoup.parse(html);

        // 输出
        sys.out.println(doc.toString());
    }

}
