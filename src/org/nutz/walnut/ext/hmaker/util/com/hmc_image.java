package org.nutz.walnut.ext.hmaker.util.com;

import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

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
        NutMap cssImg = new NutMap();
        // 处理图像的宽高
        String objectFit = ing.propCom.getString("objectFit");
        if (!"fill".equals(objectFit)) {
            cssImg.put("objectFit", objectFit);
        }
        // 如果设置了宽高，则需要指定图片的自适应
        if (!Hms.isUnset(ing.cssEle.getString("width"))) {
            cssImg.put("width", "100%");
        }
        if (!Hms.isUnset(ing.cssEle.getString("height"))) {
            cssImg.put("height", "100%");
        }
        // 提取出 arean 和 section 的属性
        String arenaKeys = "^(border.*|boxShadow|background|width|height)$";
        NutMap cssTxt = ing.cssArena.pickAndRemoveBy(Pattern.compile(arenaKeys), true);

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
