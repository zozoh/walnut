package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_navmenu extends AbstractCom {

    @Override
    protected void _exec(HmPageTranslating ing) {
        // 设置块熟悉
        ing.addMyRule(null, ing.cssArena);

        // 解包
        ing.eleCom.child(0).child(0).unwrap();
        ing.eleCom.child(0).unwrap();
        ing.eleCom.addClass("hmc-navmenu");

        // 得到当前页面的链接，已经页面的名称，记录到配置信息里
        ing.propCom.put("srcPath", "/" + ing.getRelativePath(ing.oSrc));
        ing.propCom.put("srcName", ing.oSrc.name());

        // 得到所有的菜单项
        Elements lis = ing.eleCom.getElementsByTag("li").removeAttr("open-sub");

        // 区域显示
        if (ing.propCom.is("atype", "toggleArea")) {
            lis.removeAttr("newtab");
        }
        // 默认是 链接模式
        else {
            // 修改链接项目标签
            for (Element li : lis) {
                // 得到信息
                String href = li.attr("href");
                Element a = li.child(0);
                href = ing.explainLink(href, false);
                String newtab = li.attr("newtab");

                // 链接
                if (!Strings.isBlank(href))
                    a.attr("href", href);

                // 新窗口
                if (!Strings.isBlank(newtab)) {
                    a.attr("target", "_blank");
                    li.removeAttr("newtab");
                }
            }
        }

        // 链接控件的运行时行为
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_navmenu.js");
        ing.addScriptOnLoadf("$('#%s').hmc_navmenu(%s);",
                             ing.comId,
                             Json.toJson(ing.propCom,
                                         JsonFormat.compact()
                                                   .setQuoteName(false)
                                                   .setIgnoreNull(true)));

    }

}
