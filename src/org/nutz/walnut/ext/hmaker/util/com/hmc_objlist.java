package org.nutz.walnut.ext.hmaker.util.com;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.template.HmTemplate;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_objlist extends AbstractComHanlder {

    static class TCC {
        // 代码模板
        String itemHtml;

        // 基于这个条件进行查询
        NutMap match;

        // 需要加入的 API
        NutMap api;
    }

    @Override
    protected void _exec(HmPageTranslating ing) {
        // JS 控件的配置项目
        NutMap conf = ing.prop;

        // 生成 DOM 结构
        ing.eleCom.append("<div class=\"hmc-objlist hmc-dds\"><div class=\"hmc-objlist-list\"></div></div>");

        // 得到 api 的URL
        String API = "/api/" + ing.oHome.d1();
        if (conf.has("api")) {
            String apiUrl = API + conf.getString("api");
            conf.put("apiUrl", apiUrl);
        }

        // 得到模板信息
        if (conf.has("template")) {
            String templateName = conf.getString("template");
            HmTemplate tmpl = ing.getTemplate(templateName);

            // 链接模板文件
            ing.jsLinks.add(ing.rootPath + "template/" + tmpl.info.name + ".js");

            // 读取模板信息
            conf.put("tmplInfo", tmpl.info);

            // 模板的配置信息
            Map<String, Object> options = conf.getAs("options", NutMap.class);
            if (null != options)
                options.put("API", API);
            else
                options = Lang.map("API", API);
            conf.put("options", options);

            // 模板的类选择器
            if (ing.hasSkin())
                conf.put("skinSelector", ing.skinInfo.getSkin(templateName));

        }

        // 生成 JS 代码片段，并计入转换上下文
        String script = String.format("$('#%s .hmc-objlist-list').objlist(%s);",
                                      ing.comId,
                                      Json.toJson(conf, JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        // 链接必要的外部文件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_objlist.js");
    }

}