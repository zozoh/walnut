package com.site0.walnut.ext.data.vcode.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.vcode.WnVCodeService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class vcode_check implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 得到路径和验证码
        String vcodePath = Wn.appendPath("/var/vcode", hc.params.val_check(0));
        String code = hc.params.val_check(1);

        // 获取服务类
        WnVCodeService vcodes = hc.ioc.get(WnVCodeService.class);

        // 执行验证
        if (!vcodes.check(vcodePath, code)) {
            throw Er.create("e.cmd.vcode.check_fail");
        }
    }

}
