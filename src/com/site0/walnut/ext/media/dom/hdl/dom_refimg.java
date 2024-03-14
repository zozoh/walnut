package com.site0.walnut.ext.media.dom.hdl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.ext.media.dom.DomContext;
import com.site0.walnut.ext.media.dom.DomFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class dom_refimg extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        // 获取原始路径
        String fromPath = params.check("from");
        WnObj oFromHtml = Wn.checkObj(sys, fromPath);

        // 获取新的目标路径
        String toPath = params.check("to");
        WnObj oToHtml = Wn.checkObj(sys, toPath);

        // 获取选择器
        String[] selectors = params.vals;
        if (null == selectors || selectors.length == 0) {
            selectors = Wlang.array("img.wn-media");
        }

        // 逐个选择器获取
        for (String selector : selectors) {
            List<CheapElement> els = fc.doc.selectAll(selector);
            for (CheapElement el : els) {
                // 看看对象的 src 形态是否为 /o/content?str=xxx
                String src = el.attr("src");
                if (null == src) {
                    continue;
                }
                Matcher m = P.matcher(src);
                if (!m.find()) {
                    continue;
                }
                String srcId = m.group(1);

                // 看看是否有对象 ID
                String oid = el.attr("wn-obj-id");
                if (Ws.isBlank(oid)) {
                    continue;
                }

                // 获取对应的对象
                WnObj oSrc = sys.io.get(srcId);
                String rph = Wn.Io.getRelativePath(oFromHtml, oSrc);
                WnObj oImg = sys.io.fetch(oToHtml, rph);

                // 防守（可能是目标丢失）
                if (null == oImg) {
                    continue;
                }
                // 得到新的 ID
                String toId = oImg.id();
                //sys.out.printlnf("%s: %s -> %s", srcId, rph, toId);

                // 进行改写
                String toSrc = "/o/content?str=id:" + toId;
                el.attr("src", toSrc);
                el.attr("wn-obj-id", toId);
                el.attr("wn-obj-sha-1", oImg.sha1());
                el.attr("wn-obj-mime", oImg.mime());

                // 如果有 mceSrc ，那么也要设置一下
                String mceSrc = el.attr("data-mce-src");
                if (!Ws.isBlank(mceSrc)) {
                    el.attr("data-mce-src", toSrc);
                }

            }
        }
    }

    private static final Pattern P = Pattern.compile("^/o/content\\?str=id:(.+)$");

}
