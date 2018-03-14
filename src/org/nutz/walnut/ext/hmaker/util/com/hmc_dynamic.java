package org.nutz.walnut.ext.hmaker.util.com;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.nutz.walnut.ext.hmaker.template.HmTmplField;
import org.nutz.walnut.ext.hmaker.util.HmComHandler;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.ext.hmaker.util.bean.HmApiParamField;
import org.nutz.walnut.ext.hmaker.util.bean.HmcDynamicScriptInfo;

public class hmc_dynamic extends AbstractNoneValueCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-dynamic";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        // 必须得有皮肤
        if (!ing.hasSkin())
            return false;

        // 从皮肤得到模板
        String skin = ing.eleCom.attr("skin");
        if (Strings.isBlank(skin))
            return false;

        // 得到模板的信息
        Matcher m = Pattern.compile("^skin-dynamic-([^-]+)-(.+)$").matcher(skin);
        if (!m.find())
            return false;

        String templateName = m.group(1) + "/" + m.group(2);
        HmTemplate tmpl = ing.getTemplate(templateName);
        if (null == tmpl)
            return false;

        // 设置内容
        HmcDynamicScriptInfo si = this.__setup_dynamic_content(ing);

        // 初始化服务器端数据
        __add_data_script(ing, eleArena, si);

        // 采用指定的 wnml 代码模板
        if (tmpl.hasDom()) {
            String html = __gen_customized_dom_rendering(ing, tmpl);
            eleArena.append(html);
        }
        // 直接输出裸数据
        else {
            Element eleRaw = eleArena.appendElement("script");
            eleRaw.attr("type", "text/x-template").addClass("dynamic-raw-data");
            eleRaw.append("${" + ing.comId + "(json:cqn)?-obj-}");
        }

        // ...........................................
        // 确保页面输出是 wnml
        ing.markPageAsWnml();

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hm_runtime.js");
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_dynamic.js");

        String dataKey = tmpl.info.getDomDataKey(null);
        String dataArgs = "";
        if (null != dataKey) {
            if ("@".equals(dataKey))
                dataArgs = ", ${" + ing.comId + "(json:cqn)?-obj-}";
            else
                dataArgs = ", ${" + ing.comId + "." + dataKey + "(json:cqn)?-obj-}";
        }

        String script = String.format("$('#%s > .hmc-dynamic').hmc_dynamic(%s%s);",
                                      ing.comId,
                                      Json.toJson(ing.propCom,
                                                  JsonFormat.forLook().setIgnoreNull(false)),
                                      dataArgs);
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
            String k = key;

            // 判断是否标识不要输出成动态占位符
            boolean asTmpl = true;
            if (k.startsWith("-")) {
                asTmpl = false;
                k = k.substring(1);
            }

            // 变量名
            if ("@@".equals(k)) {
                v = varName;
            }
            // 从变量里取值
            else if (k.startsWith("@@=")) {
                String vk = k.substring(3);
                String vph = options.getString(vk, "_");
                v = __gen_val(varName, vph, asTmpl);
            }
            // 固定从变量里取值
            else if (k.startsWith("@@")) {
                String vk = k.substring(2);
                v = __gen_val(varName, vk, asTmpl);
            }
            // 选项里面的键（支持 =xxx 模式）
            else if (k.startsWith("@")) {
                String vph = k.substring(1);
                String v_fmt = null;
                // ${@displayText.pubat%date:yyyy-MM-dd?}
                // 这种形式的占位符，需要被替换成
                // ${xxx.lm(date:yyyy-MM-dd)?}
                int pos = vph.indexOf('%');
                if (pos > 0) {
                    v_fmt = vph.substring(pos + 1, vph.length() - 1);
                    vph = vph.substring(0, pos);
                }
                // 得到配置的值
                Object o_v = Mapl.cell(options, vph);
                if (null != o_v) {
                    String str = o_v.toString();
                    // =th_nm : 动态从对象里取值
                    if (str.startsWith("=")) {
                        String vph2 = str.substring(1);
                        if (null != v_fmt) {
                            vph2 += "(" + v_fmt + ")";
                        }
                        // 嗯，生成动态值
                        v = __gen_val(varName, vph2, asTmpl);
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
                v = options.getString(k);
            }

            // 计入到上下文
            if (null != v)
                c.put(key, v);
        }
        // 输出
        return tmpl.dom.render(c, true);
    }

    private Object __gen_val(String varName, String vph, boolean asTmpl) {
        Object v;
        v = varName + "." + vph;
        if (asTmpl)
            v = "${" + v + "?}";
        return v;
    }

    /**
     * 力图生成这样的命令结构
     * 
     * <pre>
     * echo '${params(json:cqn)?-obj-}' \
     *      | json -a '{th_cate:"A"}' \
     *      | json -u '{pid:"vei7j2ci7mgb9qp4c6a9udm30s",pn:1,pgsz:10,skip:0}' \ 
     *      | httpapi invoke '/thing/query' -get @pipe
     * </pre>
     * 
     * @param ing
     *            运行时
     * @param eleArena
     *            Arena 元素
     * @param si
     *            脚本信息
     */
    private void __add_data_script(HmPageTranslating ing,
                                   Element eleArena,
                                   HmcDynamicScriptInfo si) {
        String api = ing.propCom.getString("api");
        Element eleDscript = eleArena.appendElement("script");
        eleDscript.addClass("wn-datasource").attr("name", ing.comId).attr("type", "json");

        // 准备生成命令
        List<String> cmds = new ArrayList<String>(si.appends.size() + 3);
        JsonFormat jfmt = JsonFormat.compact().setQuoteName(false).setIgnoreNull(false);

        // 动态读取参数
        if (si.loadRequest)
            cmds.add("echo '${params(json:cqn)?-obj-}'");

        // 追加默认值
        for (NutMap append : si.appends) {
            if (null != append && append.size() > 0) {
                cmds.add("json -a '" + Json.toJson(append, jfmt) + "'");
            }
        }

        // 一定要更新的值
        if (null != si.update && si.update.size() > 0) {
            cmds.add("json -u '" + Json.toJson(si.update, jfmt) + "'");
        }

        // 最后调用命令
        cmds.add("httpapi invoke '" + api + "' -get @pipe");

        // 加入 DOM
        eleDscript.text(Strings.join(" | ", cmds));
    }

    /**
     * @param ing
     *            运行时上下文
     * @return 格式化后用来生成服务器运行脚本的参数表, null 表示本控件内容无效
     */
    private HmcDynamicScriptInfo __setup_dynamic_content(HmPageTranslating ing) {
        NutMap com = ing.propCom;
        // 没模板，删掉
        String templateName = com.getString("template");
        if (Strings.isBlank(templateName))
            return null;

        // 没 API，删掉
        String api = ing.propCom.getString("api");
        if (null == api)
            return null;

        // 得到 API 信息
        WnObj oApi = ing.getApiObj(api);
        if (null == oApi)
            return null;

        // 记入 API 信息
        com.put("apiInfo", oApi.pick("params", "api_method", "api_return"));

        // 得到 api 的URL
        String API = "/api";
        if (com.has("api")) {
            String apiUrl = API + com.getString("api");
            com.put("apiUrl", apiUrl);
        }
        com.put("apiDomain", ing.domainName);

        // 得到 api 提交时的参数上下文
        com.put("paramContext",
                Lang.map("siteName", ing.oHome.name()).setv("siteId", ing.oHome.id()));

        // 得到模板信息
        HmTemplate tmpl = ing.getTemplate(templateName);

        // 链接模板文件
        ing.jsLinks.add(ing.rootPath + "template/" + tmpl.info.name + ".js");

        // 读取模板信息
        com.put("tmplInfo", tmpl.info);

        // 准备返回的参数表
        HmcDynamicScriptInfo hdsi = new HmcDynamicScriptInfo();

        // 格式化参数表
        NutMap apiParams = oApi.getAs("params", NutMap.class);
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

                    // 解析参数表，生成服务器端 httpapi invoke 调用参数
                    Matcher m = Pattern.compile("^([@#])(<(.+)>)?(.*)$").matcher(str);

                    // 特殊格式的参数
                    if (m.find()) {
                        String p_tp = m.group(1);
                        String p_val = m.group(3);
                        String p_arg = Strings.trim(m.group(4));

                        // 动态参数: "@<id>qcpb4e7l72h09p9na2hpo8vcue"
                        if ("@".equals(p_tp)) {
                            hdsi.update.put(key, "${params." + p_val + "?" + p_arg + "}");
                        }
                        // 来自控件，譬如 "#<filter_1>"
                        else if ("#".equals(p_tp)) {
                            // TODO 得到控件的值
                            this.__load_com_val(ing, p_val, key, hdsi);

                            // 解析参数，得到映射信息
                            __do_params_mapping(apiParams, key, hdsi.update);
                            for (NutMap append : hdsi.appends)
                                __do_params_mapping(apiParams, key, append);
                        }
                    }
                    // 其他就是静态参数
                    else {
                        hdsi.update.put(key, str);
                    }
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
            // 找到所有 mapping 类型字段，试图填充默认值
            List<String> mappingKeys = tmpl.info.getFieldByType("mapping");
            for (String mappingKey : mappingKeys) {
                Object mapping = options.get(mappingKey);
                if (null == mapping) {
                    HmTmplField fld = tmpl.info.getField(mappingKey);
                    options.put(mappingKey, fld.mapping);
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

        // 返回成功
        return hdsi;
    }

    @Override
    public void joinParamList(Element eleCom, List<String> list) {
        NutMap com = Hms.loadProp(eleCom, "hm-prop-com", false);
        if (null == com || com.size() == 0)
            return;

        NutMap params = com.getAs("params", NutMap.class);
        if (null == params || params.size() == 0)
            return;

        // 处理每个参数
        for (String key : params.keySet()) {
            Object val = params.get(key);
            if (null != val && val instanceof CharSequence) {
                String str = Strings.trim(val.toString());

                // 解析参数，看看是不是动态的
                Matcher m = Pattern.compile("^([@])(<(.+)>)?(.*)$").matcher(str);

                // 动态参数: "@<id>qcpb4e7l72h09p9na2hpo8vcue"
                if (m.find()) {
                    String p_val = m.group(3);
                    list.add(p_val);
                }
            }
        }
    }

    private void __do_params_mapping(NutMap apiParams, String key, NutMap map) {
        if (null != apiParams && map.size() > 0) {
            Object apiParamField = apiParams.get(key);
            HmApiParamField fld = parseParamFieldSetting(apiParamField);
            if (null != fld.mapping) {
                NutMap map2 = new NutMap();
                for (Map.Entry<String, Object> en : fld.mapping.entrySet()) {
                    Object v2 = map.get(en.getValue());
                    map2.put(en.getKey(), v2);
                }
                map.clear();
                map.putAll(map2);
            }
        }
    }

    private void __load_com_val(HmPageTranslating ing,
                                String comId,
                                String key,
                                HmcDynamicScriptInfo hdsi) {
        Element taCom = ing.doc.getElementById(comId);

        if (null == taCom)
            return;

        // 根据组件类型得到实例
        String ctype = taCom.attr("ctype");
        HmComHandler com = Hms.COMs.check(ctype);

        // 返回控件的值
        com.loadValue(taCom, key, hdsi);
    }

    /**
     * 解析动态设置的 setting 对象，
     * 
     * @param value
     *            参数格式为 <code>[*][(参数显示名)]@类型[=默认值][:参数[{映射表}][#注释]]</code>
     * 
     * @return 函数返回（基本符合 form 控件的field定义）:
     * 
     *         <pre>
     {
         type     : "thingset",  // 项目类型
         arg      : "xxx",       // 项目参数
         dft      : "xxx",       // 项目默认值
         mapping  : {..}         // 映射表（基本只有@com类型才会有用）
         required : true,        // 字段是否必须
         key      : "xxx",       // 字段名
         title    : "xxx",       // 字段显示名
         tip      : "xxx",       // 提示信息
     }
     *         </pre>
     */
    @SuppressWarnings({"unchecked"})
    private static HmApiParamField parseParamFieldSetting(Object value) {
        HmApiParamField fld;

        // 本来就是 Map
        if (value instanceof Map) {
            NutMap map = NutMap.WRAP((Map<String, Object>) value);
            fld = Lang.map2Object(map, HmApiParamField.class);
        }
        // 从字符串构建
        else {
            fld = new HmApiParamField();

            Pattern p = Pattern.compile("^([*])?(\\(([^\\)]+)\\))?@(input|thingset|site|com|link)(=([^:#{]*))?(:([^#{]*))?(\\{[^}]*\\})?(#(.*))?$");
            Matcher m = p.matcher(value.toString());

            if (m.find()) {
                fld.required = !Strings.isBlank(m.group(1));
                fld.title = m.group(3);
                fld.type = m.group(4);
                fld.dft = m.group(6);
                fld.arg = m.group(8);
                fld.tip = m.group(11);

                String json = m.group(9);
                if (!Strings.isBlank(json))
                    fld.mapping = Json.fromJson(NutMap.class, json);
            }
        }
        return fld;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return true;
    }

}
