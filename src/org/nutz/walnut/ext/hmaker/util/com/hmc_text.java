package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_text extends AbstractSimpleCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-text";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        // 得到编辑的文本，并将文本转义成 HTML (markdown) 模式
        String code = ing.propCom.getString("code");
        String html = code.replace("<", "&lt;")
                          .replace(">", "&gt;")
                          .replaceAll("(\r?\n){2,}", "<p>")
                          .replaceAll("\r?\n", "<br>");

        // 更新 HTML
        eleArena.html(html);

        return true;
    }

}
