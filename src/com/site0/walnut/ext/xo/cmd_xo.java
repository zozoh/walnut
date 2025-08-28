package com.site0.walnut.ext.xo;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.xo.impl.CosXoService;
import com.site0.walnut.ext.xo.impl.S3XoService;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class cmd_xo extends JvmFilterExecutor<XoContext, XoFilter> {

    public cmd_xo() {
        super(XoContext.class, XoFilter.class);
    }

    // private static final Log log = Wlog.getCMD();

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(quiet|ajax|json)$");
    }

    @Override
    protected XoContext newContext() {
        return new XoContext();
    }

    @Override
    protected void prepare(WnSystem sys, XoContext fc) {
        // 读取配置文件
        String str = fc.params.val(0);
        String[] ss = Ws.splitIgnoreBlank(str, ":");
        if (ss.length != 2) {
            throw Er.create("e.cmd.xo.setup", str);
        }
        String mode = ss[0];
        String confName = ss[1];
        fc.quiet = fc.params.is("quiet");

        // S3
        if ("s3".equalsIgnoreCase(mode)) {
            fc.api = new S3XoService(sys.io, sys.getHome(), confName);
        }
        // COS
        else if ("cos".equalsIgnoreCase(mode)) {
            fc.api = new CosXoService(sys.io, sys.getHome(), confName);
        }
    }

    @Override
    protected void output(WnSystem sys, XoContext fc) {
        if (fc.quiet) {
            return;
        }
        JsonFormat jfmt = Cmds.gen_json_format(fc.params);
        String json;
        // AJAX
        if (fc.params.is("ajax")) {
            AjaxReturn re = Ajax.ok().setData(fc.result);
            json = Json.toJson(re, jfmt);
        }
        // JSON
        else {
            json = Json.toJson(fc.result, jfmt);

        }
        sys.out.println(json);
    }

}
