package org.nutz.walnut.ext.hmaker.util.com;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.template.HmTemplate;
import org.nutz.walnut.ext.hmaker.util.HmComHandler;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

/**
 * 所有控件类处理类的基类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractComHanlder implements HmComHandler {

    @Override
    public void invoke(HmPageTranslating ing) {
        // 读取属性
        ing.prop.clear();
        Hms.fillProp(ing.prop, ing.eleCom, "hmc-prop-ele").remove();

        // 设置 arena
        ing.eleArena = ing.eleCom.children().first();

        // 记录当前控件的 ID
        ing.comId = ing.prop.getString("_id");

        // 调用子类
        this._exec(ing);

    }

    protected abstract void _exec(HmPageTranslating ing);

    private static final Pattern _P_D_PARAMS = Pattern.compile("^@([\\w\\d_-]+)(<(.+)>)?$");

    protected void _setup_dynamic_content(HmPageTranslating ing, NutMap conf) {
        // 得到 api 的URL
        String API = "/api/" + ing.oHome.d1();
        if (conf.has("api")) {
            String apiUrl = API + conf.getString("api");
            conf.put("apiUrl", apiUrl);
        }

        // 格式化参数表 @xx<xxxx> 格式的数据替换成 ${params.xx} 的动态语法
        NutMap params = conf.getAs("params", NutMap.class);
        if (null != params && params.size() > 0) {
            for (String key : params.keySet()) {
                Object val = params.get(key);
                if (null != val && val instanceof CharSequence) {
                    String str = Strings.trim(val.toString());
                    Matcher m = _P_D_PARAMS.matcher(str);
                    if (m.find()) {
                        String str2 = "${params." + m.group(1) + "?" + m.group(3) + "}";
                        params.put(key, str2);
                    }
                }
            }
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

}
