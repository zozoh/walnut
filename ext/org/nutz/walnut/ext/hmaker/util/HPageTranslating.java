package org.nutz.walnut.ext.hmaker.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;

public class HPageTranslating extends HmContext {

    private WnObj oSrc;

    private WnObj oTa;

    public HPageTranslating(HmContext hpc) {
        super(hpc);
    }

    private void __do_com(Element ele) {
        // 不是组件，就移除
        if (!ele.hasClass("hm-com")) {
            ele.remove();
            return;
        }

    }

    public WnObj translate(WnObj oSrc) {
        // 检查，源必须是个页面
        if (!oSrc.isFILE()) {
            throw Er.create("e.hmaker.page.srcMustBeFile");
        }
        this.oSrc = oSrc;

        // 准备目标

        // 解析页面
        String html = io.readText(oSrc);
        Document doc = Jsoup.parse(html);

        // 清空页面的头
        doc.head().empty();

        // 处理
        Elements eles = doc.body().children();
        for (Element ele : eles) {
            __do_com(ele);
        }

        return oDest;
    }
}
