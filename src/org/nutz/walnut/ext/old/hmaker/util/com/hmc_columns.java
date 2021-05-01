package org.nutz.walnut.ext.old.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.old.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.old.hmaker.util.Hms;

public class hmc_columns extends AbstractLayoutCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-columns";
    }

    @Override
    protected boolean _is_defined_size_max_value(HmPageTranslating ing) {
        return !Hms.isUnset(ing.cssEle.getString("width"));
    }

    @Override
    protected void __apply_area_size(Element eleArea, String asize) {
        if (Strings.isBlank(asize)) {
            asize = eleArea.attr("area-size");
        }
        if (Strings.isBlank(asize))
            return;
        // 紧凑格式
        if ("compact".equals(asize)) {
            eleArea.attr("style", "flex:0 0 auto;");
        }
        // 设置数字
        else if (asize.matches("^([\\d.]+)(px|rem|%)?$")) {
            eleArea.attr("style", "width:" + asize + ";flex:0 0 auto;");
        }
    }

}
