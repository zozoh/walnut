package com.site0.walnut.impl.box.cmd;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

@IocBean
public class cmd_wget extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) {
        // 分析参数
        ZParams params = ZParams.parse(args, null);

        // 得到目标 URL
        String url = params.val_check(0);

        // 得到输出文件
        String phTa = params.get("O");
        if (Strings.isBlank(phTa)) {
            int pos = url.lastIndexOf('/');
            if (pos > 0) {
                phTa = url.substring(pos + 1);
            } else {
                phTa = "index.html";
            }
            // 去掉参数
            pos = phTa.indexOf('?');
            if (pos > 0) {
                phTa = phTa.substring(0, pos);
            }
        }
        String aph = Wn.normalizeFullPath(phTa, sys);
        WnObj oTa = sys.io.createIfNoExists(sys.getCurrentObj(), aph, WnRace.FILE);

        // 发起请求
        Response resp = Http.get(url);
        if (!resp.isOK()) {
            throw Er.create("e.cmd.badargs");
        }
        int sz = resp.getHeader().getInt("Content-Length", -1);
        if (sz > -1) {
            sys.out.println("size=" + sz);
        }
        sys.io.writeAndClose(oTa, resp.getStream());
    }

}