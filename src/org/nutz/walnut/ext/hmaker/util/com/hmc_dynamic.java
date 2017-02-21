package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.template.HmTemplate;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_dynamic extends AbstractComHanlder {

    @Override
    protected void _exec(HmPageTranslating ing) {
        // 应用块 CSS
        ing.addMyRule(null, ing.cssBlock);

        // JS 控件的配置项目
        NutMap conf = ing.propCom;

        // 确保页面输出是 wnml
        ing.markPageAsWnml();

        // 清除 DOM 结构
        ing.eleCom.addClass("hmc-dynamic").empty();

        // 设置内容
        this._setup_dynamic_content(ing, conf);

        // 生成 JS 代码片段，并计入转换上下文
        String script = String.format("$('#%s').hmc_dynamic(%s);",
                                      ing.comId,
                                      Json.toJson(conf, JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        // 链接必要的外部文件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_dynamic.js");
    }

    private void _setup_dynamic_content(HmPageTranslating ing, NutMap conf) {
        // 得到 api 的URL
        String API = "/api/" + ing.oHome.d1();
        if (conf.has("api")) {
            String apiUrl = API + conf.getString("api");
            conf.put("apiUrl", apiUrl);
        }

        // 格式化参数表
        NutMap params = conf.getAs("params", NutMap.class);
        if (null != params && params.size() > 0) {
            // 静态替换的上下文
            NutMap pc = Lang.map("siteName", ing.oHome.name());
            pc.put("siteId", ing.oHome.id());

            // 处理每个参数
            for (String key : params.keySet()) {
                Object val = params.get(key);
                if (null != val && val instanceof CharSequence) {
                    String str = Strings.trim(val.toString());
                    // 所有 ${xxx} 进行静态替换
                    str = Tmpl.exec(str, pc);
                    // 其他加入参数表
                    params.put(key, str);
                }
            }
            // 计入参数
            conf.put("params", params);
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
            NutMap options = conf.getAs("options", NutMap.class);
            if (null != options)
                options.put("API", API);
            else
                options = Lang.map("API", API);
            conf.put("options", options);

            // 模板的类选择器
            if (ing.hasSkin())
                conf.put("skinSelector", ing.skinInfo.getSkin(templateName));

        }
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return true;
    }

}
