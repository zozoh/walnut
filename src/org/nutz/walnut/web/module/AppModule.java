package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.*;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.web.filter.WnCheckSession;

@IocBean
@At("/a")
@Filters(@By(type = WnCheckSession.class))
public class AppModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    @Inject("java:$conf.getInt('box-alloc-timeout')")
    private int allocTimeout;

    @At("/open/**")
    @Ok("jsp:jsp.app")
    @Fail("jsp:jsp.show_text")
    public Object open(String str) throws UnsupportedEncodingException {
        throw Lang.noImplement();
    }

    @At("/load/?/**")
    @Ok("raw:application/x-javascript")
    public View load(String appName, String rsName) {
        throw Lang.noImplement();
    }

    @At("/run/?/*")
    @Ok("void")
    public void run(String appName,
                    String mimeType,
                    HttpServletRequest req,
                    HttpServletResponse resp) throws IOException {

        // 得到命令行
        String cmd = Strings.trim(URLDecoder.decode(req.getQueryString(), "UTF-8"));

        // 默认返回的 mime-type 是文本
        if (Strings.isBlank(mimeType))
            mimeType = "text/plain";
        resp.setContentType(mimeType);

        // 得到一个沙箱
        WnBox box = boxes.alloc(allocTimeout);

        if (log.isDebugEnabled())
            log.debugf("box:alloc: %s", box.id());

        // 保存到请求属性中，box.onClose 的时候会删除这个属性
        req.setAttribute(WnBox.class.getName(), box);

        // 设置沙箱
        WnBoxContext bi = new WnBoxContext();

        if (log.isDebugEnabled())
            log.debugf("box:setup: %s", bi);

        box.setup(bi);

        // 准备回调
        if (log.isDebugEnabled())
            log.debug("box:set stdin/out/err");

        OutputStream ops = Streams.buff(resp.getOutputStream());

        box.setStdin(null); // HTTP GET 方式，不支持沙箱的 stdin
        box.setStdout(ops);
        box.setStderr(ops);

        // 运行
        if (log.isDebugEnabled())
            log.debugf("box:run: %s", cmd);
        box.run(cmd);

        // 等待沙箱运行结束
        if (log.isDebugEnabled())
            log.debug("box:waitForClose");
        box.waitForIdle();

        // 释放沙箱
        if (log.isDebugEnabled())
            log.debugf("box:free: %s", box.id());
        boxes.free(box);

        if (log.isDebugEnabled())
            log.debug("box:done");
    }

}
