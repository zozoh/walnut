package org.nutz.walnut.util.tmpl;

import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.tmpl.segment.AbstractTmplSegment;

/**
 * 支持循环和判断分支版的模板
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnTmplX extends AbstractTmplSegment {

    public static WnTmplX parse(String input) {
        char[] cs = input.toCharArray();
        WnTmplParsing ing = new WnTmplParsing();
        return ing.parse(cs);
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
        StringBuilder sb = new StringBuilder();
        this.renderTo(context, showKey, sb);
        return sb.toString();
    }

}
