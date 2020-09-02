package org.nutz.walnut.validate.match;

import org.nutz.walnut.validate.WnMatch;

/**
 * 根据一个字符串，自动判断是用正则还是用字符串精确匹配
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AutoStrMatch implements WnMatch {

    private WnMatch m;

    public AutoStrMatch(CharSequence cs) {
        String str = cs.toString();
        // 正则表达式
        if (str.startsWith("!^") || str.startsWith("^")) {
            this.m = new RegexMatch(str);
        }
        // 通配符
        else if (str.contains("*")) {
            this.m = new WildcardMatch(str);
        }
        // 默认就是字符串匹配
        else {
            this.m = new StringMatch(str);
        }
    }

    @Override
    public boolean match(Object val) {
        return m.match(val);
    }

}
