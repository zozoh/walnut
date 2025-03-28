package com.site0.walnut.ext.data.pvg;

import com.site0.walnut.login.WnLoginOptions;

public class WnAuthOptions extends WnLoginOptions {

    /**
     * 为 HttpApi 设计的 pvg_setup，在 HttpApiModule 里 读取信息时，解析 Json 的时候，用本类，一边读取额外的
     * pvgSetup 选项
     * 
     * 参看:
     * <ul>
     * <li>c1-regapi.md 描述的 <code>"pvg-setup"</code>
     * <li><code></code>
     * </ul>
     * 
     * @see com.site0.walnut.ext.data.pvg.BizPvgService
     * @see <code>c1-regapi.md</code> 权限验证关于 "pvg-setup" 的描述
     */
    public String pvgSetup;
}
