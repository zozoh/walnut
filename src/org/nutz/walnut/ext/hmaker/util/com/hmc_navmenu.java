package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_navmenu extends AbstractComHanlder {

    private static final NutMap _flex = Lang.map("left:'flex-start',center:'center',right:'flex-end'");

    @Override
    protected void _exec(HmPageTranslating ing) {
        String mode = ing.propPage.getString("mode", "default");

        // 准备 CSS
        NutMap rule = new NutMap();

        // 垂直模式的对齐方式
        String align = ing.propPage.getString("itemAlign", "left");
        if ("aside".equals(mode)) {
            rule.put("text-align", align);
        }
        // 默认水平方式的对齐方式
        else {
            rule.put("justify-content", _flex.getString(align, "flex-start"));
        }

        // 修改链接项目标签
        Elements lis = ing.eleCom.getElementsByTag("li");
        for (Element li : lis) {
            Element a = li.child(0);
            String href = li.attr("href");
            href = ing.explainLink(href);
            a.attr("href", href);
            if ("yes".equals(li.attr("newtab")))
                a.attr("target", "_blank");
            li.removeAttr("newtab").removeAttr("href");
        }

        // 设置
        ing.addMyCss(Lang.map(">.hmc-navmenu>ul", rule));
    }

}
