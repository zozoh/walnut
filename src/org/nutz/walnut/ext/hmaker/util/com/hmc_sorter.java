package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_sorter extends AbstractSimpleCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-sorter";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        eleArena.addClass("hmc-cnd");
        ing.eleCom.attr("wn-rt-jq-fn", "hmc_sorter");
        ing.eleCom.attr("wn-rt-jq-selector", ">.hmc-sorter");
        // ...........................................
        // 确保页面输出是 wnml
        ing.markPageAsWnml();

        // 存储默认值
        String dfv = ing.propCom.getString("defaultValue");
        if (!Strings.isBlank(dfv))
            ing.eleCom.attr("default-value", dfv);

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_sorter.js");
        String script = String.format("$('#%s > .hmc-sorter').hmc_sorter(%s);",
                                      ing.comId,
                                      Json.toJson(ing.propCom,
                                                  JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return true;
    }

    @Override
    public Object getValue(Element eleCom) {
        return eleCom.attr("default-value");
    }

}
