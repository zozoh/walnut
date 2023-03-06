package org.nutz.walnut.util.bean.val;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.bean.WnValue;
import org.nutz.walnut.util.bean.WnValues;

public class WnArrayValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input) {
        if (null == input) {
            return vd.getDefaultAs();
        }
        // 字符串的话，就拆分一下
        if (input instanceof String) {
            String sep = vd.getSeparator();
            if(Ws.isBlank(sep)) {
                return Wlang.list(input);
            }
            List<String> ss = Ws.splitIgnoreBlanks((String) input, sep);
            if (ss.isEmpty()) {
                return vd.getEmptyAs();
            }
            return ss;
        }
        // 深度递归一下
        List<Object> list = new LinkedList<>();
        Wlang.each(input, (index, o, src) -> {
            // 指定了子元素，深层转换一下
            WnValue eleType = vd.getEleType();
            if (null != eleType) {
                Object v = WnValues.toValue(eleType, o);
                list.add(v);
            }
            // 就保持原始值咯
            else {
                list.add(o);
            }
        });
        if (list.isEmpty()) {
            return vd.getEmptyAs();
        }
        return list;
    }

    // @Override
    // public String toStr(WnValue vd, Object val) {
    // List<String> ss = new LinkedList<>();
    // Wlang.each(val, (index, o, src) -> {
    // String s = WnValues.toStr(vd, o);
    // ss.add(s);
    // });
    // return Ws.join(ss, vd.getSeparator());
    // }

}
