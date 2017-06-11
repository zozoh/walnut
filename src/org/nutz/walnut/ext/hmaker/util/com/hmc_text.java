package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Tag;
import org.nutz.plugins.zdoc.markdown.Markdown;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_text extends AbstractNoneValueCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-text";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        // 得到编辑的文本，并将文本转义成 HTML (markdown) 模式
        String code = ing.propCom.getString("code", "");

        String html;

        // 如果包括换行，则表示是 markdown 文本
        if (code.contains("\n")) {
            html = Markdown.toHtml(code, new Callback<Tag>() {
                public void invoke(Tag tag) {}
            });
        }
        // 否则就是纯文本
        else {
            html = Strings.escapeHtml(code);
        }

        // 更新 HTML
        eleArena.appendElement("article").addClass("md-content").html(html);

        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }
}
