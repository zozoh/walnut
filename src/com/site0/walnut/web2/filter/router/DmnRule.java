package com.site0.walnut.web2.filter.router;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.tmpl.WnTmpl;

/**
 * hostmap 的一条重写规则：正则匹配 + 模板渲染（对照旧 {@code WalnutFilter.DmnMatcher}）。
 */
public class DmnRule {

    private final Pattern regex;

    private final WnTmpl tmpl;

    private DmnRule(Pattern regex, WnTmpl tmpl) {
        this.regex = regex;
        this.tmpl = tmpl;
    }

    public static DmnRule of(String regex, String tmpl) {
        return new DmnRule(Pattern.compile(regex), WnTmpl.parse(tmpl));
    }

    /**
     * 按路径渲染新路径；不匹配返回 <code>null</code>。
     */
    public String render(String path, String grp) {
        Matcher m = regex.matcher(path);
        if (!m.find())
            return null;
        NutMap vars = new NutMap();
        vars.put("grp", null == grp ? "" : grp);
        for (int i = 0; i <= m.groupCount(); i++)
            vars.put("" + i, m.group(i));
        return tmpl.render(vars);
    }

}
