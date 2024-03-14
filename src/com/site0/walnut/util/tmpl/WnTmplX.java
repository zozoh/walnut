package com.site0.walnut.util.tmpl;

import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.util.tmpl.segment.AbstractTmplSegment;

/**
 * 支持循环和判断分支版的模板
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnTmplX extends AbstractTmplSegment {

    public static WnTmplX parse(WnTmplElementMaker tknMaker,
                                WnTmplTokenExpert expert,
                                String input) {
        char[] cs = input.toCharArray();
        WnTmplParsing ing = new WnTmplParsing(tknMaker);
        ing.setExpert(expert);
        return ing.parse(cs);
    }

    public static WnTmplX parse(String input) {
        return parse(null, null, input);
    }

    public static WnTmplX parsef(String fmt, Object... args) {
        if (null == fmt)
            return null;
        String input = String.format(fmt, args);
        return parse(input);
    }

    public static String exec(String tmpl, NutBean context) {
        WnTmplX x = parse(tmpl);
        return x.render(context, true);
    }

    /**
     * @see #exec(String, Pattern, int, int, NutBean, boolean)
     */
    public static String exec(String tmpl, NutBean context, boolean showKey) {
        WnTmplX x = parse(tmpl);
        return x.render(context, showKey);
    }

    public String render(NutBean context) {
        return render(context, false);
    }

    public String render(NutBean context, boolean showKey) {
        WnTmplRenderContext rc = new WnTmplRenderContext(context);
        rc.showKey = showKey;
        this.renderTo(rc);
        return rc.out.toString();
    }

}
