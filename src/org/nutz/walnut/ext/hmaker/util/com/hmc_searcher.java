package org.nutz.walnut.ext.hmaker.util.com;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.ext.hmaker.util.bean.HmcDynamicScriptInfo;

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
    public void loadValue(Element eleCom, String key, HmcDynamicScriptInfo hdsi) {
        String dfv = eleCom.attr("default-value");
        if (!Strings.isBlank(dfv)) {
            Matcher m = _P.matcher(dfv);
            // 动态从参数里读取
            if (m.find()) {
                hdsi.update.put(key, "${params." + m.group(1) + "?}");
            }
            // 填写默认值
            else {
                hdsi.update.put(key, dfv);
            }
        }
    }

    @Override
    public void joinParamList(Element eleCom, List<String> list) {
        NutMap propCom = Hms.loadProp(eleCom, "hm-prop-com", false);
        String dfv = propCom.getString("defaultValue");
        if (!Strings.isBlank(dfv)) {
            Matcher m = _P.matcher(dfv);
            if (m.find()) {
                list.add(m.group(1));
            }
        }
    }
}
