package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;

public class hmc_rows extends AbstractLayoutCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-rows";
    }

    @Override
    protected boolean _is_defined_size_max_value(HmPageTranslating ing) {
        return !Hms.isUnset(ing.cssEle.getString("height"));
    }

    @Override
    protected void __apply_area_size(Element eleArea, String asize) {
        if (Strings.isBlank(asize)) {
            asize = eleArea.attr("area-size");
        }

        if (!Strings.isBlank(asize)) {
            eleArea.attr("style", "height:" + asize + ";");
            eleArea.child(0).attr("style", "height:100%;");
        }
    }

}
