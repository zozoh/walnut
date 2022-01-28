package org.nutz.walnut.ext.data.o.impl.pop;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.nutz.walnut.ext.data.o.util.WnPop;

/**
 * <ul>
 * <li><code>3</code> : 从后面弹出最多三个
 * <li><code>-1</code> : 从开始处弹出最多一个
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class PopN implements WnPop {

    private int n;

    public PopN(int n) {
        this.n = n;
    }

    @Override
    public <T extends Object> List<T> exec(List<T> list) {
        if (null == list) {
            return null;
        }
        Iterator<T> it = list.iterator();
        int len = list.size();
        // 从后面
        if (n > 0) {
            List<T> re = new ArrayList<>(list.size());
            int lastI = Math.max(0, len - n);
            int i = 0;
            while (i < lastI && it.hasNext()) {
                T li = it.next();
                re.add(li);
                i++;
            }
            return re;
        }
        // 从前面
        else if (n < 0) {
            List<T> re = new ArrayList<>(list.size());
            int firstI = Math.abs(n);
            int i = 0;
            while (it.hasNext()) {
                T li = it.next();
                if (i >= firstI) {
                    re.add(li);
                }
                i++;
            }
            return re;
        }
        // 维持
        return list;
    }

}
