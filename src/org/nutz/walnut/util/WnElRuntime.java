package org.nutz.walnut.util;

/**
 * 对于动态 EL 表达式提供一个限制版的全局函数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnElRuntime {

    public int count(Object any) {
        return Wlang.count(any);
    }

    public int sub(Object... args) {
        if (null == args || args.length == 0) {
            return 0;
        }
        int n = Wlang.toInt(args[0]);
        for (int i = 1; i < args.length; i++) {
            int v = Wlang.toInt(args[i]);
            n -= v;
        }
        return n;
    }

}
