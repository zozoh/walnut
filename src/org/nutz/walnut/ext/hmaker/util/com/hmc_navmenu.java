package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_navmenu extends AbstractComHanlder {

    @Override
    protected void _exec(HmPageTranslating ing) {
        // 设置块熟悉
        ing.addMyRule(null, ing.cssBlock);

        // 得到所有的菜单项
        Elements lis = ing.eleCom.getElementsByTag("li").removeAttr("open-sub");

        // 区域显示
        if (ing.propCom.is("atype", "toggleArea")) {
            ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_navmenu_toggle_area.js");
            ing.addScriptOnLoadf("$('#%s').hmc_navmenu_toggle_area({target:$('#%s')});",
                                 ing.comId,
                                 ing.propCom.getString("layoutComId"));

            // 修改链接项目标签
            for (Element li : lis) {
                li.removeAttr("newtab");
            }
        }
        // 默认是 链接模式
        else {
            // 修改链接项目标签
            for (Element li : lis) {
                String href = li.attr("href");
                Element a = li.child(0);
                href = ing.explainLink(href, false);
                if (!Strings.isBlank(href))
                    a.attr("href", href);
                if ("yes".equals(li.attr("newtab")))
                    a.attr("target", "_blank");
                li.removeAttr("newtab").removeAttr("href");
            }
        }

    }

}
