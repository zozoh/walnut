package org.nutz.walnut.ext.old.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.old.hmaker.util.HmPageTranslating;

public class hmc_navmenu extends AbstractNoneValueCom {

    public hmc_navmenu() {
        super();
        this.skinAttributes = Lang.array("auto-dock");
    }

    @Override
    protected String getArenaClassName() {
        return "hmc-navmenu";
    }

    protected Element genArenaElement(HmPageTranslating ing, String arenaClassName) {
        return ing.eleCom.child(0).child(0).addClass("hmc-navmenu");
    }

    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        // 添加皮肤属性
        // for (Map.Entry<String, Object> en : ing.skinAttributes.entrySet()) {
        // eleArena.attr(en.getKey(), en.getValue().toString());
        // }

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
                // zozoh: href 就不用展开了，因为所有控件输出的结果后，最后会被统一转换的
                // href = ing.explainLink(href, false);
                String newtab = li.attr("newtab");

                // 链接
                if (!Strings.isBlank(href))
                    a.attr("href", href);

                // 新窗口
                if (!Strings.isBlank(newtab)) {
                    a.attr("target", "_blank");
                    a.attr("rel", "nofollow");
                    li.removeAttr("newtab");
                }
            }
        }

        // 如果有声明 color，则作为链接的前景色
        Object color = ing.cssArena.remove("color");
        if (null != color) {
            ing.addMySkinRule("ul li a", Lang.map("color", color));
        }

        // 链接控件的运行时行为
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_navmenu.js");
        ing.addScriptOnLoadf("$('#%s .hmc-navmenu').hmc_navmenu(%s);",
                             ing.comId,
                             Json.toJson(ing.propCom,
                                         JsonFormat.compact()
                                                   .setQuoteName(false)
                                                   .setIgnoreNull(true)));

        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }
}
