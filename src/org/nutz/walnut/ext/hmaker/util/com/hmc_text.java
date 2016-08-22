package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_text extends AbstractComHanlder {

    @Override
    protected void _exec(HmPageTranslating ing) {

        // 隐藏标题
        if (!ing.prop.getBoolean("showTitle", true)) {
            Element eleTitle = ing.eleArena.child(0);
            eleTitle.remove();
        }
        // 显示标题
        else {
            NutMap cssHeader = ing.prop.getAs("title", NutMap.class);
            ing.addMyCss(Lang.map(">.hmc-text>header", cssHeader));
        }

        // 更新内容显示
        NutMap cssContent = ing.prop.getAs("content", NutMap.class);
        ing.addMyCss(Lang.map(">.hmc-text>section", cssContent));

    }

}
