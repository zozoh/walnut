package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public abstract class AbstractLayoutCom extends AbstractCom {

    @Override
    protected void _exec(HmPageTranslating ing) {
        String arenaSelector = " > .hm-com-W > ." + this.getArenaClassName();
        ing.addMyRule(null, ing.cssEle);
        ing.addMyRule(arenaSelector, ing.cssArena);
        // ..................................................................
        // 分别设置所有区域的尺寸
        Elements eleAreaList = ing.eleCom.select(arenaSelector + " > .hm-area");

        // 特殊处理，如果只有一个区域，那么尽量将其撑满
        if (eleAreaList.size() == 1 && this._is_defined_size_max_value(ing)) {
            Element eleArea = eleAreaList.get(0);
            this.__apply_area_size(eleArea, "100%");
        }
        // 否则，重新应用一遍 AreaSize
        else {
            for (Element eleArea : eleAreaList) {
                this.__apply_area_size(eleArea, null);
            }
        }
        // ..................................................................
        // 同步皮肤属性开关
        Element eleArena = ing.eleCom.child(0).child(0);
        String skin = ing.eleCom.attr("skin");
        this.syncComSkinAttributes(ing, eleArena, skin);
    }

    protected abstract boolean _is_defined_size_max_value(HmPageTranslating ing);

    protected abstract void __apply_area_size(Element eleArea, String asize);

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }

    @Override
    public Object getValue(Element eleCom) {
        return null;
    }

}
