package org.nutz.walnut.ext.hmaker.util.com;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_image extends AbstractComHanlder {

    @Override
    protected void _exec(HmPageTranslating ing) {
        // 得到属性
        String src = ing.prop.getString("src");

        // 准备 CSS
        NutMap css = new NutMap();

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

        // 处理其他属性
        css.put("background-color", "#000");
        css.put("background-repeat", "norepeat");
        css.put("background-size", "100% 100%");
        css.put("width", "100%");
        css.put("height", "100%");

        // 设置
        String style = Hms.genCssRuleStyle(css);
        ing.eleCom.attr("style", style);

    }

}
