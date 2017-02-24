package org.nutz.walnut.ext.hmaker.util.com;

import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_text extends AbstractCom {

    @Override
    protected void _exec(HmPageTranslating ing) {
        // 修改 DOM
        ing.eleCom.child(0).child(0).unwrap();
        ing.eleCom.child(0).unwrap();
        ing.eleCom.addClass("hmc-text");

        // 得到编辑的文本，并将文本转义成 HTML (markdown) 模式
        String code = ing.propCom.getString("code");
        String html = code.replace("<", "&lt;")
                          .replace(">", "&gt;")
                          .replaceAll("(\r?\n){2,}", "<p>")
                          .replaceAll("\r?\n", "<br>");

        // 更新 HTML
        ing.eleCom.empty().html(html);
    }

}
