package org.nutz.walnut.ext.old.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.old.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.old.hmaker.util.Hms;
import org.nutz.walnut.util.Wn;

public class hmc_htmlcode extends AbstractNoneValueCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-htmlcode";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        String code = ing.propCom.getString("code", "");

        // 设置内容
        eleArena.html(code);

        // ...........................................
        // 链入控件的 jQuery 插件
        String json = Json.toJson(ing.propCom, JsonFormat.forLook().setIgnoreNull(false));
        json = Wn.escapeHtml(json, true);
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_htmlcode.js");
        String script = String.format("$('#%s').hmc_htmlcode(%s);", ing.comId, json);
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }
}