package com.site0.walnut.util.validate.impl;

import org.nutz.lang.Strings;
import com.site0.walnut.util.Wregion;
import com.site0.walnut.util.validate.WnMatch;

/**
 * 根据一个字符串，自动判断是用正则还是用字符串精确匹配
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AutoStrMatch implements WnMatch {

    private WnMatch m;

    private boolean not;

    public AutoStrMatch(CharSequence cs) {
        this(cs, false);
    }

    public AutoStrMatch(CharSequence cs, boolean not) {
        this.not = not;
        // nil
        if (null == cs) {
            this.m = new NilMatch();
        }
        // empty
        else if (Strings.isEmpty(cs) || "[EMPTY]".equals(cs)) {
            this.m = new EmptyMatch();
        }
        // 更多判断
        else {
            String str = cs.toString();
            this.m = RegexMatch.tryParse(str);

            // 正则表达式
            if (null != this.m) {
                return;
            }

            if (str.startsWith("!")) {
                this.not = !not;
                str = str.substring(1).trim();
            }
            // empty
            if (Strings.isEmpty(cs) || "[EMPTY]".equals(str)) {
                this.m = new EmptyMatch();
            }
            // BLANK
            else if (Strings.isBlank(cs) || "[BLANK]".equals(str)) {
                this.m = new BlankMatch();
            }
            // 通配符
            else if (str.contains("*")) {
                this.m = new WildcardMatch(str);
            }
            // 整数范围
            else if (str.matches(Wregion.intRegion())) {
                this.m = new LongRegionMatch(str);
            }
            // 长整数范围
            else if (str.matches(Wregion.longRegion())) {
                this.m = new LongRegionMatch(str);
            }
            // 浮点数范围
            else if (str.matches(Wregion.floatRegion())) {
                this.m = new FloatRegionMatch(str);
            }
            // 日期范围
            else if (str.matches(Wregion.dateRegion("^"))) {
                this.m = new DateTimeMatch(str);
            }
            // 默认就是字符串匹配
            else {
                this.m = new StringMatch(str);
            }

        }
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public WnMatch getMatch() {
        return this.m;
    }

    @Override
    public boolean match(Object val) {
        return m.match(val) ^ not;
    }

}
