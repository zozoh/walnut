package org.nutz.walnut.ext.hmaker.hdl;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Encoding;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnHttpResponse;

public class hmaker_read implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {

        // 得到资源路径
        String ph = hc.params.val_check(0);

        // 不允许 ph 里写 "id:xxx"，这可能是个漏洞
        if (ph.indexOf("id:") >= 0)
            throw Er.create("e.cmd.hmaker.read.DangerousPath", ph);

        // 准备返回对象
        WnObj wobj = null;

        // 如果是 _skin_var.less 则懒加载
        if ("_skin_var.less".equals(ph)) {

        }
        // 如果是 skin.css 则动态编译
        else if ("skin.css".equals(ph)) {
            // 先确保当前站点已经指定了皮肤
            // 没指定的话返回空

            // 指定的话，则看看站点的 .skin/_skin_var.less 的 ETag
            // 当然，如果没有这个文件，就从皮肤目录 copy 一个过来

            // 如果 ETag 与 .cache/skin.css 文件里面的记录的一样
            // 那么就用 skin.css 来下载

            // 否则的话，就执行命令，生成一个 skin.css
            // lessc compile ~/.hmaker/skin/default/skin.less -pri-path
            // id:siteId/.skin

            // 得到这个 skin.css 再进行后续的下载流程
        }
        // 否则，直接读取文件
        else {
            wobj = sys.io.check(hc.oRefer, ph);
        }

        // 否则直接输出
        // 准备响应对象头部
        WnHttpResponse resp = new WnHttpResponse();
        resp.setStatus(200);
        resp.setEtag(hc.params.getString("etag"));

        // 准备下载
        if (hc.params.is("download")) {
            resp.setUserAgent(hc.params.getString("UserAgent"));
        }

        // 准备文件

        String range = hc.params.getString("range");
        resp.prepare(sys.io, wobj, range);

        // 输出
        OutputStream ops = sys.out.getOutputStream();
        resp.writeTo(ops);
    }

}
