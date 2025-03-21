package com.site0.walnut.util.bean.val;

import java.util.Collection;

import org.nutz.castor.Castors;
import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.bean.WnValue;

public class WnStringValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue fld, Object input, NutBean bean) {
        if (null == input) {
            return fld.getDefaultAs();
        }
        String s;
        // 格式化
        if (fld.hasFormat()) {
            s = String.format(fld.getFormat(), input);
        }
        // 集合
        else if (input.getClass().isArray()) {
            String sep = Ws.sBlank(fld.getSeparator(), ",");
            s = Ws.join((Object[]) input, sep);
        }
        // 数组
        else if (input instanceof Collection) {
            String sep = Ws.sBlank(fld.getSeparator(), ",");
            s = Ws.join((Collection<?>) input, sep);
        }
        // 否则直接转字符串
        else {
            s = Castors.me().castToString(input);
        }
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
        // 转换大小写
        if (fld.hasValueCase()) {
            String caseMode = fld.getValueCase();
            s = Ws.toCase(s, caseMode);
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
