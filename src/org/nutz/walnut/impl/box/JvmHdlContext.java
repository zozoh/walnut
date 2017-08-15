package org.nutz.walnut.impl.box;

import org.nutz.ioc.Ioc;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;

public class JvmHdlContext extends NutMap {

    public Ioc ioc;

    public WnSystem sys;

    public String hdlName;

    public JvmHdl hdl;

    public String[] args;

    public ZParams params;

    public WnPager pager;

    /**
     * 通常为 cmd xxx hdl 形式的 xxx 指代的命令参考对象
     */
    public WnObj oRefer;

    public JsonFormat jfmt;

    public Object output;

    /**
     * 一些补充的信息可以放在这里
     */
    private NutMap _attrs;

    public NutMap attrs() {
        if (null == _attrs) {
            _attrs = new NutMap();
        }
        return _attrs;
    }

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
