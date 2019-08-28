package org.nutz.walnut.ext.hmaker.util.com;

import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.plugins.zdoc.markdown.Markdown;
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

        // 展开属性
        if (ing.propCom.has("textPos")) {
            eleArena.attr("tpos", ing.propCom.getString("textPos"));
        }
        if (ing.propCom.getBoolean("hoverShow")) {
            eleArena.attr("hover-show", "yes");
        } else {
            eleArena.removeAttr("hover-show");
        }

        // zozoh: src 就不用展开了，因为所有控件输出的结果后，最后会被统一转换的
        Element eleImg = eleArena.appendElement("img").attr("src", src);
        if (ing.propCom.has("alt")) {
            eleImg.attr("alt", ing.propCom.getString("alt"));
        }

        // zozoh: href 就不用展开了，因为所有控件输出的结果后，最后会被统一转换的
        String href = ing.propCom.getString("href");
        // href = ing.explainLink(href, false);
        if (!Strings.isBlank(href)) {
            ing.eleCom.attr("href", href);
        }

        // 处理打开新窗口
        if (ing.propCom.getBoolean("newtab")) {
            ing.eleCom.attr("target", "_blank");
        }

        // 文字属性
        String text = ing.propCom.getString("text");
        if (!Strings.isBlank(text)) {
            // 如果包括换行，则表示是 markdown 文本
            if (text.contains("\n")) {
                String html = Markdown.toHtml(text, null);
                eleArena.appendElement("section")
                        .appendElement("article")
                        .addClass("md-content")
                        .html(html);
            }
            // 否则就是纯文本
            else {
                eleArena.appendElement("section").text(text);
            }

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

        // ...........................................
        // 链入控件的 jQuery 插件
        ing.jsLinks.add("/gu/rs/ext/hmaker/hmc_image.js");
        String script = String.format("$('#%s > .hmc-image').hmc_image(%s);",
                                      ing.comId,
                                      Json.toJson(ing.propCom,
                                                  JsonFormat.forLook().setIgnoreNull(false)));
        ing.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));

        // 返回成功吧
        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }
}
