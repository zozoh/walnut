package com.site0.walnut.ext.net.webx.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.net.webx.WebxContext;
import com.site0.walnut.ext.net.webx.WebxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class webx_auth extends WebxFilter {

    @Override
    protected void process(WnSystem sys, WebxContext fc, ZParams params) {
        // 分析参数
        String uname = params.getString("name");
        String phone = params.getString("phone");
        String email = params.getString("email");

        // TODO 严格模式，必须需要动态密码
        // boolean isStrict = params.is("strict");

        // 自动模式，如果用户不存在，自动创建账号
        boolean autoAdd = params.is("autoadd");

        try {
            // 优先静态密码登录：手机号/邮箱/登录名
            if (params.has("passwd")) {
                String nameOrPhoneOrEmail = uname;
                if (Ws.isBlank(nameOrPhoneOrEmail)) {
                    nameOrPhoneOrEmail = Ws.sBlank(phone, email);
                }
                String rawPasswd = params.getString("passwd");
                fc.result = fc.api.loginByPassword(nameOrPhoneOrEmail, rawPasswd);
            }
            // 采用微信小程票据登录
            else if (params.has("wxmp")) {
                String code = params.getString("wxmp");
                fc.result = fc.api.loginByWechatMPCode(code, autoAdd);
            }
            // 报错，不支持的登录形式
            else {
                fc.error = Er.create("e.cmd.webx.auth.NoSupportForm");
            }
        }
        catch (Throwable e) {
            fc.error = Er.wrap(e);
        }
    }

}
