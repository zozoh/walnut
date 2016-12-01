package org.nutz.walnut.ext.hmaker.util.com;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.template.HmTemplate;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public abstract class AbstractDynamicContentCom extends AbstractComHanlder {
    private static final Pattern _P_D_PARAMS = Pattern.compile("^@([\\w\\d_-]+)(<(.+)>)?$");

    protected void _setup_dynamic_content(HmPageTranslating ing, NutMap conf) {
        ing.markPageAsWnml();
        
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

    @Override
    public boolean isDynamic(Element eleCom) {
        return true;
    }
}
