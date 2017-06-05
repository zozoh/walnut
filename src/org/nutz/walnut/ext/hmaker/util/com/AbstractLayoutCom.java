package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public abstract class AbstractLayoutCom extends AbstractCom {

    @Override
    protected void _exec(HmPageTranslating ing) {
        ing.addMyRule(null, ing.cssEle);
        ing.addMyRule("." + this.getArenaClassName(), ing.cssArena);

        // 同步皮肤属性开关
        Element eleArena = ing.eleCom.child(0).child(0);
        String skin = ing.eleCom.attr("skin");
        this.syncComSkinAttributes(ing, eleArena, skin);
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }

    @Override
    public Object getValue(Element eleCom) {
        return null;
    }

}
