package org.nutz.walnut.ext.old.hmaker.util.com;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.old.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.old.hmaker.util.Hms;
import org.nutz.walnut.ext.old.hmaker.util.bean.HmcDynamicScriptInfo;

public class hmc_filter extends AbstractSimpleCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-filter";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        eleArena.addClass("hmc-cnd");
        ing.eleCom.attr("wn-rt-jq-fn", "hmc_filter");
        ing.eleCom.attr("wn-rt-jq-selector", ">.hmc-filter");
        // ...........................................
        // 确保页面输出是 wnml
        ing.markPageAsWnml();

        // 存储默认值
        Object dfv = ing.propCom.get("defaultValue");
        if (null != dfv && (dfv instanceof Map<?, ?>))
            ing.eleCom.attr("default-value", Json.toJson(dfv, JsonFormat.compact()));

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_filter.js");
        String script = String.format("$('#%s > .hmc-filter').hmc_filter(%s);",
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
    public void loadValue(Element eleCom, String key, HmcDynamicScriptInfo hdsi) {
        String json = eleCom.attr("default-value");
        if ("yes".equals(eleCom.attr("hm-loadreq"))) {
            hdsi.loadRequest = true;
        }
        // 默认值
        if (!Strings.isBlank(json)) {
            hdsi.appends.add(Json.fromJson(NutMap.class, json));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void joinParamList(Element eleCom, List<String> list) {
        if ("yes".equals(eleCom.attr("hm-loadreq"))) {
            NutMap propCom = Hms.loadProp(eleCom, "hm-prop-com", false);
            List<Map<String, Object>> fields = propCom.getAs("fields", List.class);
            for (Map<String, Object> fld : fields) {
                Object name = fld.get("name");
                if (null != name)
                    list.add(name.toString());
            }
        }
    }
}