package org.nutz.walnut.ext.hmaker.util.com;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_searcher extends AbstractSimpleCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-searcher";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        ing.eleCom.attr("wn-rt-jq-fn", "hmc_searcher");
        ing.eleCom.attr("wn-rt-jq-selector", ">.hmc-searcher");
        // ...........................................
        // 确保页面输出是 wnml
        ing.markPageAsWnml();

        // 存储默认值
        String dfv = ing.propCom.getString("defaultValue");
        if (!Strings.isBlank(dfv))
            ing.eleCom.attr("default-value", dfv);

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_searcher.js");
        String script = String.format("$('#%s > .hmc-searcher').hmc_searcher(%s);",
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

    private final Pattern _P = Pattern.compile("^@<([^>]+)>$");

    @Override
    public Object getValue(Element eleCom) {
        String val = eleCom.attr("default-value");
        Matcher m = _P.matcher(val);
        if (m.find()) {
            return "${params." + m.group(1) + "?}";
        }
        return val;
    }

}
