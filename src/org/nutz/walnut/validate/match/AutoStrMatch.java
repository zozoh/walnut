package org.nutz.walnut.validate.match;

import org.nutz.lang.Strings;
import org.nutz.walnut.util.WnRg;
import org.nutz.walnut.validate.WnMatch;

/**
 * 根据一个字符串，自动判断是用正则还是用字符串精确匹配
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AutoStrMatch implements WnMatch {

    private WnMatch m;

    public AutoStrMatch(CharSequence cs) {
        // null
        if (null == cs) {
            this.m = new NullMatch();
        }
        // empty
        else if (Strings.isEmpty(cs)) {
            this.m = new EmptyMatch();
        }
        // blank
        else if (Strings.isBlank(cs) || "[BLANK]".equals(cs)) {
            this.m = new BlankMatch();
        }
        // 更多判断
        else {
            String str = cs.toString();
            // 正则表达式
            if (str.startsWith("!^") || str.startsWith("^")) {
                this.m = new RegexMatch(str);
            }
            // 通配符
            else if (str.contains("*")) {
                this.m = new WildcardMatch(str);
            }
            // 整数范围
            else if (str.matches(WnRg.intRegion())) {
                this.m = new IntRegionMatch(str);
            }
            // 长整数范围
            else if (str.matches(WnRg.longRegion())) {
                this.m = new LongRegionMatch(str);
            }
            // 浮点数范围
            else if (str.matches(WnRg.floatRegion())) {
                this.m = new FloatRegionMatch(str);
            }
            // 日期范围
            else if (str.matches(WnRg.dateRegion("^"))) {
                this.m = new DateTimeMatch(str);
            }
            // 默认就是字符串匹配
            else {
                this.m = new StringMatch(str);
            }
        }
    }
    
    public WnMatch getMatch() {
        return this.m;
    }

    @Override
    public boolean match(Object val) {
        return m.match(val);
    }

}
