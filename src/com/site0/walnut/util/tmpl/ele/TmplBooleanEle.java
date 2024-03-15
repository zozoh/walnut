package com.site0.walnut.util.tmpl.ele;

import org.nutz.castor.Castors;
import org.nutz.lang.Strings;
import com.site0.walnut.util.Wlang;

public class TmplBooleanEle extends TmplDynamicEle {

    // [false, true]
    private String[] texts;

    public TmplBooleanEle(String key, String fmt, String dft) {
        super("boolean", key, fmt, dft);
        if (Strings.isBlank(fmt)) {
            this.texts = Wlang.array("false", "true");
        }
        // 定制了
        else {
            String s = Strings.sNull(fmt, "false/true");
            int pos = s.indexOf('/');
            // "xxx"
            if (pos < 0) {
                texts = Wlang.array("", s.trim());
            }
            // "/xxx"
            else if (pos == 0) {
                texts = Wlang.array("", s.substring(pos + 1).trim());
            }
            // "xxx/"
            else if (pos == s.length() - 1) {
                texts = Wlang.array(s.substring(0, pos).trim(), "");
            }
            // must by "xxx/xxx"
            else {
                texts = Wlang.array(s.substring(0, pos).trim(), s.substring(pos + 1).trim());
            }
        }
    }

    @Override
    protected String _val(Object val) {
        boolean b = false;
        if (null != val) {
            b = Castors.me().castTo(val, Boolean.class);
        }
        return b ? texts[1] : texts[0];
    }

}
