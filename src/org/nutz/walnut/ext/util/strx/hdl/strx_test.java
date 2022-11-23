package org.nutz.walnut.ext.util.strx.hdl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.ext.util.strx.StrXContext;
import org.nutz.walnut.ext.util.strx.StrXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class strx_test extends StrXFilter {

    @Override
    protected void process(WnSystem sys, StrXContext fc, ZParams params) {
        String regex = params.val_check(0);
        boolean not = false;
        if (regex.startsWith("!")) {
            not = true;
            regex = regex.substring(1);
        }
        Pattern p = Regex.getPattern(regex);
        Matcher m = p.matcher(fc.data);

        if (m.find() ^ not) {
            // 准备上下文
            NutMap c = new NutMap();

            // 嗯，not=true 时， m 应该是 null 吧
            if (null != m) {
                c.put("val", fc.data);
                int len = m.groupCount();
                for (int i = 0; i <= len; i++) {
                    String s = m.group(i);
                    c.put("g" + i, s);
                }
            }

            // 准备输出: 标准
            if (params.has("out")) {
                Tmpl tmpl = Tmpl.parse(params.getString("out"));
                fc.data = tmpl.render(c);
            }
            // 准备输出: 错误
            else if (params.has("err")) {
                Tmpl tmpl = Tmpl.parse(params.getString("err"));
                fc.data = tmpl.render(c);
                fc.error = true;
            }
        }
    }

}
