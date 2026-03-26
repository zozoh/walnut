package com.site0.walnut.util.tmpl.ele;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.lang.Strings;

import com.site0.walnut.util.bank.HDirecton;
import com.site0.walnut.util.bank.PartitionOptions;
import com.site0.walnut.util.bank.Wbank;

public class TmplFloatEle extends TmplDynamicEle {

    private DecimalFormat decFormat;

    private PartitionOptions _bank_options;

    private static Pattern _C = Pattern
        .compile("^#(-)?([^#]+)(#*)[.](#*)(%\\.\\d+f)$");

    public TmplFloatEle(String key, String fmt, String dft) {
        super("float", key, fmt, dft);

        // 这里有一个对于货币的快速设置方法
        // '#,###.##%.2f' 相当于
        // {width:3,sep:',',decimalPlaces:2} + '%.2f
        // 譬如:
        // |-----------------------------------------------------------
        // | /^#(-)?([^#])(#*)[.](#*)(%\.\d+f)$/.exec('#,###.##%.2f')
        // |-----------------------------------------------------------
        // | 0: "#,###.##%.2f"
        // | 1: undefined
        // | 2: ","
        // | 3: "###"
        // | 4: "##"
        // | 5: "%.2f"
        // | groups: undefined
        // | index: 0
        // | input: "#,###.##%.2f"
        // | length: 6
        if (null != fmt) {
            Matcher m = _C.matcher(fmt);
            if (m.find()) {
                this._bank_options = new PartitionOptions();
                this._bank_options.width = m.group(3).length();
                this._bank_options.sep = m.group(2);
                this._bank_options.decimalPlaces = m.group(4).length();
                this._bank_options.to = "-".equals(m.group(1)) ? HDirecton.right
                                                               : HDirecton.left;
                fmt = "0.##";
            }
        }

        this.fmt = Strings.sNull(fmt, "0.##");
        // 采用普通的格式化，那么就不需要 decFormat
        if (this.fmt.indexOf('%') >= 0) {

        }
        // 数字格式化
        else {
            this.decFormat = new DecimalFormat(this.fmt);
        }
    }

    @Override
    protected String _val(Object val) {
        Float n = Castors.me().castTo(val, Float.class);
        if (null != n) {
            if (null != decFormat) {
                String re = decFormat.format(n);
                if (null != _bank_options) {
                    String r2 = Wbank.toBankText(re, _bank_options);
                    return r2;
                }
                return re;
            }
            return String.format(this.fmt, n);
        }
        return null;
    }

}
