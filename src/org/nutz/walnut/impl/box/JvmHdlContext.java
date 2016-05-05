package org.nutz.walnut.impl.box;

import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.ZParams;

public class JvmHdlContext extends NutMap {

    public WnSystem sys;

    public String hdlName;

    public JvmHdl hdl;

    public String[] args;

    public ZParams params;

    public WnObj oHome;

    public JsonFormat jfmt;

    public void parseParams(String[] args) {
        // 得到注解
        JvmHdlParamArgs jhpa = null;

        if (null != this.hdl)
            jhpa = this.hdl.getClass().getAnnotation(JvmHdlParamArgs.class);

        // 解析
        this.params = ZParams.parse(args, null == jhpa ? null : jhpa.value());
    }

}
