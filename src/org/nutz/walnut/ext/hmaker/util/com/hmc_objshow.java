package org.nutz.walnut.ext.hmaker.util.com;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_objshow extends AbstractDynamicContentCom {

    @Override
    protected void _exec(HmPageTranslating ing) {
        // JS 控件的配置项目
        NutMap conf = ing.propPage;

        // 确保页面输出是 wnml
        ing.markPageAsWnml();

        // 生成 DOM 结构
        ing.eleCom.append("<div class=\"hmc-objshow hmc-dds\"></div>");

        this._setup_dynamic_content(ing, conf);

        // 得到 api 的URL
        if (ing.propPage.has("api")) {
            String apiUrl = "/api/" + ing.oHome.d1() + conf.getString("api");
            conf.put("apiUrl", apiUrl);
        }

        // 生成 JS 代码片段，并计入转换上下文
        String script = String.format("$('#%s > .hmc-objshow').objshow(%s);",
                                      ing.comId,
                                      Json.toJson(conf, JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        // 链接必要的外部文件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_objshow.js");
    }

}
