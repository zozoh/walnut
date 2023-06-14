package org.nutz.walnut.util.tmpl;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.tmpl.segment.AbstractTmplSegment;

/**
 * 支持循环和判断分支版的模板
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnTmplX extends AbstractTmplSegment {

    public String render(NutBean context) {
        return render(context, false);
    }

    public String render(NutBean context, boolean showKey) {
        StringBuilder sb = new StringBuilder();
        this.renderTo(context, showKey, sb);
        return sb.toString();
    }

}
