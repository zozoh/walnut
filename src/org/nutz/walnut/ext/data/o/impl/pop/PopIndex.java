package org.nutz.walnut.ext.data.o.impl.pop;

import java.util.List;

import org.nutz.walnut.ext.data.o.util.WnPop;

/**
 * <ul>
 * <li><code>i3</code> : 0 base下标，即第四个
 * <li><code>i-1</code> : 最后一个
 * <li><code>i-2</code> : 倒数第二个
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class PopIndex implements WnPop {

    private int index;

    public PopIndex(int index) {
        this.index = index;
    }

    @Override
    public List<Object> pop(List<Object> list) {
        if (null == list || list.isEmpty()) {
            return null;
        }
        int len = list.size();
        int theI = index;
        // 从后面
        if (index < 0) {
            theI = len + index;
        }
        // 别越界
        theI = Math.max(0, Math.min(theI, len - 1));
        // 移除
        list.remove(theI);
        return list;
    }

}
