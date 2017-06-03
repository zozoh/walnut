package org.nutz.walnut.ext.hmaker.util.com;

import java.util.List;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.template.HmTemplate;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_dynamic extends AbstractSimpleCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-dynamic";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        // 设置内容
        if (!this._setup_dynamic_content(ing))
            return false;

        // 初始化服务器端数据
        __add_data_script(ing, eleArena);

        // 得到模板的信息
        String templateName = ing.propCom.getString("template");
        HmTemplate tmpl = ing.getTemplate(templateName);

        // 采用指定的 wnml 代码模板
        if (tmpl.hasDom()) {
            String html = __gen_customized_dom_rendering(ing, tmpl);
            eleArena.append(html);
        }

        // 直接输出裸数据

        // ...........................................
        // 确保页面输出是 wnml
        ing.markPageAsWnml();

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hm_runtime.js");
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_dynamic.js");
        String script = String.format("$('#%s > .hmc-dynamic').hmc_dynamic(%s);",
                                      ing.comId,
                                      Json.toJson(ing.propCom,
                                                  JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        return true;
    }

    private String __gen_customized_dom_rendering(HmPageTranslating ing, HmTemplate tmpl) {
        // 得到 @ 表示的页面运行时变量名，默认为组件 ID
        String varName = tmpl.info.getDomVarName(ing.comId);

        // 得到控件的模板配置信息
        NutMap options = ing.propCom.getAs("options", NutMap.class);

        // 那么这个模板对应的数据对象变量是什么呢？
        // 如果是列表需要循环的通常占位符写做 ${@@}
        // 而 ${@th_nm} 通常表示
        // 准备上下文
        NutMap c = new NutMap();
        c.put("comId", ing.comId);

        // 循环判断占位符
        for (String key : tmpl.dom.keys()) {
            Object v = null;
            // 变量名
            if ("@@".equals(key)) {
                v = varName;
            }
            // 固定从变量里取值
            else if (key.startsWith("@@")) {
                String vk = key.substring(2);
                String vph = options.getString(vk, "_");
                v = "${" + varName + "." + vph + "}";
            }
            // 选项里面的键（支持 =xxx 模式）
            else if (key.startsWith("@")) {
                String vph = key.substring(1);
                Object o_v = Mapl.cell(options, vph);
                if (null != o_v) {
                    String str = o_v.toString();
                    // =th_nm : 动态从对象里取值
                    if (str.startsWith("=")) {
                        v = "${" + varName + "." + str.substring(1) + "?}";
                    }
                    // 就是一个静态的值
                    else {
                        v = str;
                    }
                }
                // 为空的话，用空字符串搞一下
                else {
                    v = "";
                }
            }
            // 嗯，其他就直接从选项里面取
            else {
                v = options.getString(key);
            }

            // 计入到上下文
            if (null != v)
                c.put(key, v);
        }
        // 输出
        String html = tmpl.dom.render(c, true);
        return html;
    }

    private void __add_data_script(HmPageTranslating ing, Element eleArena) {
        String api = ing.propCom.getString("api");
        Element eleDscript = eleArena.appendElement("script");
        eleDscript.addClass("wn-datasource").attr("name", ing.comId).attr("type", "json");
        eleDscript.text("httpapi invoke '" + api + "' -get 'pid:\"sf33grk0uiiqgq0g4r1c3akjdf\"'");
    }

    private boolean _setup_dynamic_content(HmPageTranslating ing) {
        NutMap com = ing.propCom;
        // 没模板，删掉
        String templateName = com.getString("template");
        if (Strings.isBlank(templateName))
            return false;

        // 没 API，删掉
        String api = ing.propCom.getString("api");
        if (null == api)
            return false;

        // 得到 API 信息
        WnObj oApi = ing.getApiObj(api);
        if (null == oApi)
            return false;

        // 记入 API 信息
        com.put("apiInfo", oApi.pick("params", "api_method", "api_return"));

        // 得到 api 的URL
        String API = "/api";
        if (com.has("api")) {
            String apiUrl = API + com.getString("api");
            com.put("apiUrl", apiUrl);
        }

        // 得到 api 提交时的参数上下文
        com.put("paramContext",
                Lang.map("siteName", ing.oHome.name()).setv("siteId", ing.oHome.id()));

        // 得到模板信息
        HmTemplate tmpl = ing.getTemplate(templateName);

        // 链接模板文件
        ing.jsLinks.add(ing.rootPath + "template/" + tmpl.info.name + ".js");

        // 读取模板信息
        com.put("tmplInfo", tmpl.info);

        // 格式化参数表
        NutMap params = com.getAs("params", NutMap.class);
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
                    // 加入参数表
                    params.put(key, str);
                }
            }

            // 计入参数
            com.put("params", params);
        }

        // 模板的配置信息
        NutMap options = com.getAs("options", NutMap.class);
        // 处理一下用户填写的 options ...
        if (null != options) {
            // 根据模板信息进行分析，找到所有的 link 类型的字段
            // 然后修改对应的参数
            List<String> linkKeys = tmpl.info.getFieldByType("link");
            for (String linkKey : linkKeys) {
                String link = options.getString(linkKey);
                if (!Strings.isBlank(link)) {
                    String lnk2 = ing.explainLink(link, false);
                    options.put(linkKey, lnk2);
                }
            }
            // 增加 API 选项
            options.put("API", API);
        }
        // 总之要加一个 API 选项
        else {
            options = Lang.map("API", API);
        }
        com.put("options", options);

        // 模板的类选择器
        if (ing.hasSkin())
            com.put("skinSelector", ing.skinInfo.getSkinForTemplate(templateName));

        // 返回成功
        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return true;
    }

}
