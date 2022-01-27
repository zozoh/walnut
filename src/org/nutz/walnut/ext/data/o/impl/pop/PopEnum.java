package org.nutz.walnut.ext.data.o.impl.pop;

import org.nutz.walnut.util.Ws;

/**
 * <ul>
 * <li><code>[a,b]</code> : 弹出半角逗号分隔的列表里的值
 * <li><code>!=xyz</code> : 弹出不在半角逗号分隔的列表里的值
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class PopEnum extends PopMatch {

    private String[] vals;

    public PopEnum(String input) {
        this.vals = Ws.splitIgnoreBlank(input);
    }

    @Override
    protected boolean isMatch(Object ele) {
        if (null != vals) {
            for (String val : vals) {
                if (ele.equals(val)) {
                    return true;
                }
            }
        }
        return false;
    }

}
