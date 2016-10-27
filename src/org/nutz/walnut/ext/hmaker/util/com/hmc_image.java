package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_image extends AbstractComHanlder {

    @Override
    protected void _exec(HmPageTranslating ing) {

        // 图片属性
        NutMap css = __gen_img_css(ing);
        ing.addMyCss(Lang.map(" ", css));

        // 文字属性
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 准备更新文本样式
        NutMap txt = ing.prop.getAs("text", NutMap.class);
        String content = null == txt ? null : txt.getString("content");
        if (null != content) {
            css = __gen_txt_css(txt);

            // 设置文本显示
            Element eleTxt = ing.eleCom.ownerDocument()
                                       .createElement("DIV")
                                       .addClass("hmc-image-txt")
                                       .text(content);
            ing.eleCom.appendChild(eleTxt);
            ing.addMyCss(Lang.map(" > .hmc-image-txt", css));
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

    private NutMap __gen_img_css(HmPageTranslating ing) {
        // 得到属性
        String src = ing.prop.getString("src");

        // 准备 CSS
        NutMap css = ing.prop.pick("width", "height");
        css.putDefault("width", "100%");
        css.putDefault("height", "100%");
        css.put("position", "relative");

        // 处理图片路径
        if (!Strings.isBlank(src)) {
            // https?:// 开头的直接抄吧
            if (src.matches("^https?://")) {
                css.put("background-image", "url('" + src + "')");
            }
            // 其他指向某图片文件
            else {
                // 得到这个图片文件
                WnObj oImg;

                // 从站点根开始
                if (src.startsWith("/")) {
                    oImg = ing.io.fetch(ing.oHome, src.substring(1));
                }
                // 否则从当前网页的目录开始查找
                else {
                    oImg = ing.io.fetch(ing.oSrc, src);
                }

                // 没找到图片，那么抱歉，抛错
                if (null == oImg) {
                    if (ing.strict)
                        throw Er.create("e.cmd.hmaker.publish.hmc_image.noexists", src);
                }
                // 找到了图片
                else {
                    String rph = ing.getRelativePath(ing.oSrc, oImg);
                    css.put("background-image", "url('" + rph + "')");
                    // 计入要 copy 的资源
                    ing.resources.add(oImg);
                }
            }
        }

        // 处理缩放
        String scale = ing.prop.getString("scale", "");
        switch (scale) {
        case "contain":
        case "cover":
            css.put("background-repeat", "no-repeat");
            css.put("background-size", scale);
            css.put("background-position", "center center");
            break;
        case "tile":
            css.put("background-repeat", "repeat");
            break;
        default:
            css.put("background-repeat", "no-repeat");
            css.put("background-size", "100% 100%");
        }
        return css;
    }

}
