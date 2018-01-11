package org.nutz.walnut.ext.hmaker.util.com;

import java.util.Map;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_pager extends AbstractSimpleCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-pager";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        ing.eleCom.attr("wn-rt-jq-fn", "hmc_pager");
        ing.eleCom.attr("wn-rt-jq-selector", ">.hmc-pager");
        // ...........................................
        // 确保页面输出是 wnml
        ing.markPageAsWnml();

        // 存储默认值
        Object dfv = ing.propCom.get("defaultValue");
        if (null != dfv && (dfv instanceof Map<?, ?>)) {
            // 更新一下指定默认值
            NutMap map = NutMap.WRAP((Map<String, Object>) dfv);
            map.put("pgsz", ing.propCom.getInt("dftPageSize", 50));

            // 存一下，以便 getValue 的时候使用
            ing.eleCom.attr("default-value", Json.toJson(map, JsonFormat.compact()));
        }
        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_pager.js");
        String script = String.format("$('#%s > .hmc-pager').hmc_pager(%s);",
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
        String json = eleCom.attr("default-value");
        if (!Strings.isBlank(json))
            return Json.fromJson(NutMap.class, json);
        return null;
    }

}
