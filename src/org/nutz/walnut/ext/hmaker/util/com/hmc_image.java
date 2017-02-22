package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_image extends AbstractCom {

    @Override
    protected void _exec(HmPageTranslating ing) {

        // 图片源
        String src = ing.propCom.getString("src");
        src = ing.explainLink(src, true);

        // 图片不存在，那么删除整个控件
        if (Strings.isBlank(src)) {
            ing.eleCom.remove();
            return;
        }

        // 建立包裹的 DIV
        Element eleDiv = ing.eleCom.child(0).addClass("hmc-image");
        eleDiv.appendElement("img").attr("src", src);

        // 图片属性
        NutMap cssImg = ing.cssBlock.pickAndRemove("width", "height", "border", "borderRadius");
        String objectFit = ing.propCom.getString("objectFit");
        if (!"fill".equals(objectFit)) {
            cssImg.put("objectFit", objectFit);
        }

        // 图片包裹
        NutMap cssArena = ing.cssBlock.pickAndRemove("background", "boxShadow");
        if (cssImg.has("borderRadius"))
            cssArena.put("borderRadius", cssImg.get("borderRadius"));

        // 增加规则
        ing.addMyRule(null, ing.cssBlock);
        ing.addMyRule(".hmc-image", cssArena);
        ing.addMyRule("img", cssImg);

        // 超链接
        String href = ing.propCom.getString("href");
        href = ing.explainLink(href, false);
        if (!Strings.isBlank(href))
            ing.eleCom.attr("href", href);

        // 文字属性
        NutMap txt = ing.propCom.getAs("text", NutMap.class);
        String content = null == txt ? null : txt.getString("content");
        if (null != content) {
            // 设置文字
            eleDiv.appendElement("section").text(content);

            // 处理 css
            NutMap cssTxt = __gen_txt_css(txt);
            ing.addMyRule("section", cssTxt);
        }
    }

    private NutMap __gen_txt_css(NutMap txt) {
        // 计算文本的 CSS
        NutMap css = Lang.map("position:'absolute'");

        // 文本位置极其宽高，根据顶底左右不同，选择 txt.size 表示的是宽还是高
        String pos = txt.getString("pos", "S");
        switch (pos) {
        // N: North 顶
        case "N":
            css.put("top", 0);
            css.put("left", 0);
            css.put("right", 0);
            css.put("bottom", "");
            css.put("width", "");
            css.put("height", txt.getString("size", ""));
            break;
        // E: Weat 左
        case "W":
            css.put("top", 0);
            css.put("left", 0);
            css.put("right", "");
            css.put("bottom", 0);
            css.put("width", txt.getString("size", ""));
            css.put("height", "");
            break;
        // E: East 右
        case "E":
            css.put("top", 0);
            css.put("left", "");
            css.put("right", 0);
            css.put("bottom", 0);
            css.put("width", txt.getString("size", ""));
            css.put("height", "");
            break;
        // 默认 S: South 底
        default:
            css.put("top", "");
            css.put("left", 0);
            css.put("right", 0);
            css.put("bottom", 0);
            css.put("width", "");
            css.put("height", txt.getString("size", ""));
        }

        // 边距等其他属性
        css.put("padding", txt.getString("padding", ""));
        css.put("color", txt.getString("color", ""));
        css.put("background", txt.getString("background", ""));
        css.put("textAlign", txt.getString("textAlign", ""));
        css.put("lineHeight", txt.getString("lineHeight", ""));
        css.put("letterSpacing", txt.getString("letterSpacing", ""));
        css.put("fontSize", txt.getString("fontSize", ""));
        css.put("textShadow", txt.getString("textShadow", ""));

        // 返回
        return css;
    }

}
