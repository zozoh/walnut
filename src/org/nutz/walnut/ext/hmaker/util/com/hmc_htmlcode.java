package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.util.Wn;

public class hmc_htmlcode extends AbstractNoneValueCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-htmlcode";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        String code = ing.propCom.getString("code", "");

        // 转换为 HTML 代码
        String html = Wn.unescapeHtml(code, false);

        // 设置内容
        eleArena.html(html);

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_htmlcode.js");
        String script = String.format("$('#%s').hmc_htmlcode(%s);",
                                      ing.comId,
                                      Json.toJson(ing.propCom,
                                                  JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }
}
