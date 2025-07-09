package com.site0.walnut.ext.net.webx;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.web.ajax.Ajax;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.maker.WnLoginApiMaker;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_webx extends JvmFilterExecutor<WebxContext, WebxFilter> {

    // private static final Log log = Wlog.getCMD();

    public cmd_webx() {
        super(WebxContext.class, WebxFilter.class);
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl");
    }

    @Override
    protected WebxContext newContext() {
        return new WebxContext();
    }

    @Override
    protected void prepare(WnSystem sys, WebxContext fc) {
        String sitePath = fc.params.val_check(0);
        WnObj oSite = Wn.checkObj(sys, sitePath);

        // 准备读取站点设置
        WnLoginOptions options;

        // 文件的话，读取内容
        if (oSite.isFILE()) {
            options = sys.io.readJson(oSite, WnLoginOptions.class);
        }
        // 否则直接采用元数据
        else {
            options = Wlang.map2Object(oSite, WnLoginOptions.class);
        }

        // 创建权鉴接口
        fc.api = WnLoginApiMaker.forDomain().make(sys.io, sys.session.getEnv(), options);
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
