package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_image extends AbstractNoneValueCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-image";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        // ...........................................
        // 处理 DOM: 图片源
        String src = ing.propCom.getString("src");

        // 图片不存在，那么删除整个控件
        if (Strings.isBlank(src)) {
            return false;
        }

        // 展开链接
        src = ing.explainLink(src, true);

        eleArena.appendElement("img").attr("src", src);

        // 超链接
        String href = ing.propCom.getString("href");
        href = ing.explainLink(href, false);
        if (!Strings.isBlank(href))
            ing.eleCom.attr("href", href);

        // 文字属性
        String text = ing.propCom.getString("text");
        if (!Strings.isBlank(text)) {
            eleArena.appendElement("section").text(text);
        }

        // ..........................................
        // 处理 CSS
        // 图片属性
        NutMap cssImg = ing.cssEle.pickAndRemove("border", "borderRadius", "width", "height");
        String objectFit = ing.propCom.getString("objectFit");
        if (!"fill".equals(objectFit)) {
            cssImg.put("objectFit", objectFit);
        }

        // 文本属性
        NutMap cssTxt = ing.cssArena.pickAndRemove("color", "background", "padding");

        // 如果图片有了圆角，那么考虑到文字，要为圆角属性加回去
        if (cssImg.has("borderRadius"))
            ing.cssArena.put("borderRadius", cssImg.get("borderRadius"));

        // 增加图片的规则
        ing.addMyRule("img", cssImg);
        ing.addMyRule("section", cssTxt);

        // 返回成功吧
        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }
}
