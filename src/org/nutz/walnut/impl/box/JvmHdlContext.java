package org.nutz.walnut.impl.box;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

public class JvmHdlContext extends NutMap {

    public WnSystem sys;

    public String hdlName;

    public JvmHdl hdl;

    public String[] args;

    public ZParams params;

    public WnPager pager;

    public WnObj oHome;

    public JsonFormat jfmt;

    public Object output;

    public void parseParams(String[] args) {
        // 得到注解
        JvmHdlParamArgs jhpa = null;

        if (null != this.hdl)
            jhpa = this.hdl.getClass().getAnnotation(JvmHdlParamArgs.class);

        // 解析
        if (null == jhpa) {
            this.params = ZParams.parse(args, null);
        }
        // 自动模式
        else if (Strings.isBlank(jhpa.regex())) {
            this.params = ZParams.parse(args, jhpa.value());
        }
        // 单独指定了正则表达式
        else {
            this.params = ZParams.parse(args, jhpa.value(), jhpa.regex());
        }
    }

}
