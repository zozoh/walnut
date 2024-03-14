package com.site0.walnut.ext.data.o.impl.pop;

import com.site0.walnut.util.Ws;

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
        if (null != vals && vals.length > 0) {
            for (String val : vals) {
                if (ele.equals(val)) {
                    return true;
                }
            }
        }
        return false;
    }

}
