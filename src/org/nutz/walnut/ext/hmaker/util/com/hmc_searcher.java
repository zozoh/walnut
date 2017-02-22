package org.nutz.walnut.ext.hmaker.util.com;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_searcher extends AbstractCom {

    @Override
    protected void _exec(HmPageTranslating ing) {
        // ...........................................
        // 处理 DOM
        ing.eleCom.child(0).child(0).unwrap();
        ing.eleCom.child(0).unwrap();
        ing.eleCom.addClass("hmc-searcher");

        // ...........................................
        // 确保页面输出是 wnml
        ing.markPageAsWnml();

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_searcher.js");
        String script = String.format("$('#%s').hmc_searcher(%s);",
                                      ing.comId,
                                      Json.toJson(ing.propCom,
                                                  JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));
    }

}
