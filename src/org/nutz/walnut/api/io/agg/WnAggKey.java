package org.nutz.walnut.api.io.agg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.util.Ws;

public abstract class WnAggKey {

    private static Pattern _P = Pattern.compile("^([^=]+)(=(([^:]+):)?(.+))?");

    public static WnAggKey parse(String input) {
        WnAggKey re = null;
        Matcher m = _P.matcher(input);
        if (m.find()) {
            String toName = Ws.trim(m.group(1));
            String funcName = Ws.toUpper(Ws.trim(m.group(4)));
            String fromName = Ws.trim(m.group(5));
            if (Ws.isBlank(fromName)) {
                fromName = toName;
            }

            // 指明了方法名，看看是不是计算键呢？
            if (!Ws.isBlank(funcName)) {
                try {
                    WnAggFunc func = WnAggFunc.valueOf(funcName);
                    WnAggregateKey ck = new WnAggregateKey();
                    ck.setFunc(func);
                    re = ck;
                }
                catch (Exception e) {}
            }

            // 看来不是计算键，尝试分组键
            if (null == re) {
                WnAggGroupKey gk = new WnAggGroupKey();
                if (!Ws.isBlank(funcName)) {
                    WnAggTransMode mode = WnAggTransMode.valueOf(funcName);
                    gk.setFunc(mode);
                }
                re = gk;
            }

            re.fromName = fromName;
            re.funcName = funcName;
            re.toName = toName;
        }
        return re;

    }

    /**
     * 在表（视图）中的键名
     */
    protected String fromName;

    /**
     * 要执行的计算或者转换函数名称
     */
    protected String funcName;

    /**
     * 计算后的值存储的键名
     */
    protected String toName;

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

}
