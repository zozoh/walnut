package com.site0.walnut.ext.net.webx;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.web.ajax.Ajax;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_webx extends JvmFilterExecutor<WebxContext, WebxFilter> {

    // private static final Log log = Wlog.getCMD();

    public cmd_webx() {
        super(WebxContext.class, WebxFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl", "^(ajax)$");
    }

    @Override
    protected WebxContext newContext() {
        return new WebxContext();
    }

    @Override
    protected void prepare(WnSystem sys, WebxContext fc) {
        String sitePath = fc.params.val_check(0);
        String aph = Wn.normalizeFullPath(sitePath, sys);
        fc.site = WnLoginSite.createByPath(sys.io, aph);
        fc.api = fc.site.auth();
    }

    @Override
    protected void output(WnSystem sys, WebxContext fc) {
        JsonFormat jfmt = Cmds.gen_json_format(fc.params);
        boolean asAjax = fc.params.is("ajax");

        // 静默输出
        if (fc.quiet) {
            return;
        }

        Object out;
        // 错误输出
        if (null != fc.error) {
            out = fc.error;
            if (asAjax) {
                out = Ajax.fail().setErrCode(fc.error.getKey()).setData(fc.error.getReason());
            }
        }
        // 正常输出
        else {
            out = fc.result;
            if (asAjax) {
                out = Ajax.ok().setData(fc.result);
            }
        }
        String json = Json.toJson(out, jfmt);
        sys.out.println(json);
    }
}
