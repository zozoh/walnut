package org.nutz.walnut.util.bean.val;

import org.nutz.castor.Castors;
import org.nutz.walnut.util.bean.WnValue;

public class WnStringValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue fld, Object input) {
        if (null == input) {
            return fld.getDefaultAs();
        }
        String s = Castors.me().castToString(input);
        // 成对替换
        if (fld.hasReplace()) {
            String[] reps = fld.getReplace();
            // 偶数个
            int n = reps.length / 2;
            for (int i = 0; i < n; i++) {
                int off = i * 2;
                String r0 = reps[off];
                String r1 = reps[off + 1];
                s = s.replaceAll(r0, r1);
            }
            // 最后一个
            int remain = reps.length - (n * 2);
            if (remain > 0) {
                s = s.replaceAll(reps[reps.length - 1], "");
            }
            // 成对
        }
        // 格式化
        if (fld.hasFormat()) {
            s = String.format(fld.getFormat(), s);
        }
        // 搞定
        return s;
    }

    // @Override
    // public String toStr(WnValue fld, Object val) {
    // if (fld.hasFormat()) {
    // return String.format(fld.getFormat(), val);
    // }
    // return Castors.me().castToString(val);
    // }

}
