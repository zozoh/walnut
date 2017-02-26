package org.nutz.walnut.ext.hmaker.util.com;

import java.util.Map;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_image extends AbstractCom {

    @Override
    protected void _exec(HmPageTranslating ing) {
        // ...........................................
        // 处理 DOM
        ing.eleCom.empty().addClass("hmc-image");

        // 图片源
        String src = ing.propCom.getString("src");
        src = ing.explainLink(src, true);

        // 图片不存在，那么删除整个控件
        if (Strings.isBlank(src)) {
            ing.eleCom.remove();
            return;
        }

        ing.eleCom.appendElement("img").attr("src", src);

        // 超链接
        String href = ing.propCom.getString("href");
        href = ing.explainLink(href, false);
        if (!Strings.isBlank(href))
            ing.eleCom.attr("href", href);

        // 文字属性
        String text = ing.propCom.getString("text");
        if (!Strings.isBlank(text)) {
            ing.eleCom.appendElement("section").text(text);
        }

        // ..........................................
        // 处理 CSS
        // 图片属性
        NutMap cssImg = ing.cssArena.pickAndRemove("width", "height", "border", "borderRadius");
        String objectFit = ing.propCom.getString("objectFit");
        if (!"fill".equals(objectFit)) {
            cssImg.put("objectFit", objectFit);
        }

        // 如果图片有了圆角，那么也应该为顶级元素增加圆角
        if (cssImg.has("borderRadius"))
            ing.cssArena.put("borderRadius", cssImg.get("borderRadius"));

        // 处理所有的皮肤属性，同时生成纯 css
        NutMap cssArena = new NutMap();
        for (Map.Entry<String, Object> en : ing.cssArena.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            // 增加属性
            if (key.startsWith("sa-") && null != val) {
                ing.eleCom.attr(key, val.toString());
            }
            // 记录成 css
            else {
                cssArena.put(key, val);
            }
        }

        // 增加规则
        ing.addMyRule(null, cssArena);
        ing.addMyRule("img", cssImg);

    }

}
