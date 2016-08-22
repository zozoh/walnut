package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_columns extends AbstractComHanlder {

    @Override
    protected void _exec(HmPageTranslating ing) {
        for (Element eleCol : ing.eleArena.children()) {
            String width = Strings.sBlank(eleCol.attr("col-b-width"), "auto");
            if (!"auto".equals(width)) {
                String css = String.format("width:%s;flex:0 0 auto;", width);
                eleCol.attr("style", css);
            }
        }
    }

}
