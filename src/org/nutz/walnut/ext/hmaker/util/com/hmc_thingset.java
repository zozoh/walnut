package org.nutz.walnut.ext.hmaker.util.com;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.web.WebException;

public class hmc_thingset extends AbstractComHanlder {

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
        // 逐个生成 thingset 的各个部分
        this.__paint_filter(ing);
        this.__paint_item(ing);
        this.__paint_pager(ing);

        // 链接必要的外部文件
        ing.cssLinks.add("/gu/rs/ext/hmaker/hmc_thingset.css");
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_thingset.js");
    }

    private void __paint_filter(HmPageTranslating ing) {

    }

    private void __paint_pager(HmPageTranslating ing) {

    }

    private void __paint_item(HmPageTranslating ing) {
        // 预先分析
        TCC tcc = this.__gen_context(ing);

        // 插入代码模板
        Element ele = ing.eleCom.appendElement("section");
        ele.attr("hidden", "yes").html(tcc.itemHtml);

        // JS 控件的配置项目
        NutMap conf = new NutMap();
        conf.put("api", tcc.api);
        conf.put("match", tcc.match);
        conf.put("mapping", ing.prop.get("mapping"));

        // 生成 JS 代码片段，并计入转换上下文
        String script = String.format("$('#%s').thingset(%s);",
                                      ing.comId,
                                      Json.toJson(conf, JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));
    }

    private WebException _create_err(HmPageTranslating ing, String mode) {
        return Er.create("e.cmd.hmaker.publish.hmc_thingset."
                         + mode,
                         ing.oSrc.path() + "#" + ing.prop.getString("_id"));
    }

    private TCC __gen_context(HmPageTranslating ing) {
        TCC tcc = new TCC();
        WnIo io = ing.io;

        // 必须有数据源
        String dsId = ing.prop.getString("dsId");
        if (Strings.isBlank(dsId)) {
            throw _create_err(ing, "none");
        }

        // 检查数据源格式
        Matcher m = Pattern.compile("^thing:([\\w\\d]{5,})$").matcher(dsId);
        if (!m.find()) {
            throw _create_err(ing, "invalid");
        }

        // 获取数据源
        WnObj oTS = io.get(m.group(1));
        if (null == oTS) {
            throw _create_err(ing, "gone");
        }
        tcc.match = Lang.map("th", oTS.id());
        tcc.api = Lang.map("query", "/thing/query");

        // 获取显示模板
        String template = ing.prop.getString("template");
        if (Strings.isBlank(template)) {
            throw _create_err(ing, "tmplnone");
        }

        WnObj oTmpl = io.fetch(ing.oConfHome, "template/thingset/" + template);
        if (null == oTmpl) {
            throw _create_err(ing, "tmplgone");
        }

        // 检查模板的 CSS
        WnObj oTmplCss = io.fetch(oTmpl, "css.json");
        if (null == oTmplCss) {
            throw _create_err(ing, "tmplnocss");
        }

        // 看看是否能够成功读取
        String cssJson = Strings.trim(io.readText(oTmplCss));
        if (Strings.isBlank(cssJson)) {
            throw _create_err(ing, "tmplcss_E");
        }

        // 解析，如果格式正确，加入到全局的 CSS 列表以便之后输出
        try {
            NutMap cssMap = Json.fromJson(NutMap.class, cssJson);
            ing.cssRules.put(ing.comId, cssMap);
        }
        catch (Exception e) {
            throw _create_err(ing, "tmplcss_E");
        }

        // 检查模板的 DOM
        WnObj oTmplDom = io.fetch(oTmpl, "dom.html");
        if (null == oTmplDom) {
            throw _create_err(ing, "tmplnodom");
        }

        tcc.itemHtml = Strings.trim(io.readText(oTmplDom));
        if (Strings.isBlank(tcc.itemHtml)) {
            throw _create_err(ing, "tmpldom_E");
        }

        // 返回 DOM 结构
        return tcc;

    }

}