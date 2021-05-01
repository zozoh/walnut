package org.nutz.walnut.ext.data.www.impl;

import org.nutz.lang.Strings;
import org.nutz.walnut.util.validate.impl.AutoMatch;

/**
 * 格式化虚拟页面
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class VirtualPage {

    // 用来渲染的页面，譬如 index.wnml
    // 也可以带路径，譬如 abc/index.wnml
    public String entryPath;

    // 如果 entryPath 是 abc/index.wnml
    // 这个 abc 就作为 contextName
    public String contextName;

    // 匹配列表
    AutoMatch[] matchers;

    public VirtualPage(String s) {
        int pos = s.indexOf(':');
        if (pos >= 0) {
            entryPath = Strings.sBlank(s.substring(0, pos), null);
            s = s.substring(pos + 1);
            pos = entryPath.indexOf('/');
            if (pos > 0) {
                contextName = entryPath.substring(0, pos);
            }
        }
        String[] vps = Strings.splitIgnoreBlank(s, ",");
        matchers = new AutoMatch[vps.length];
        for (int i = 0; i < vps.length; i++) {
            String vp = vps[i];
            matchers[i] = new AutoMatch(vp);
        }
    }

    // 匹配，如果 matchers 是空地，那么作废
    public boolean match(String path) {
        if (null == matchers || matchers.length == 0)
            return false;

        for (AutoMatch m : matchers)
            if (m.match(path))
                return true;

        return false;
    }

}
