package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.PUT;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView2;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.util.WnWeb;

/**
 * 提供未登录访客访问内容的方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@At("/gu")
@Filters(@By(type = WnAsUsr.class, args = {"guest", "guest"}))
public class GuestModule extends AbstractWnModule {
    
    public static View HTTP_304 = new HttpStatusView(304);

    @At("/**")
    @Fail("http:404")
    public View read(String str, HttpServletRequest req, 
                     HttpServletResponse resp) {
        WnObj o = Wn.checkObj(io, str);
        resp.setDateHeader("Last-Modified", o.lastModified());
        String sha1 = o.sha1();
        if (!Strings.isBlank(sha1))
            resp.setHeader("ETag", sha1);
        if (o.lastModified()/1000 == req.getDateHeader("If-Modified-Since")/1000) {
            if (Strings.isBlank(sha1) || sha1.equals(req.getAttribute("If-None-Match")))
                return HTTP_304;
        }
        // 返回输入流
        String contentType = o.mime();

        InputStream in = io.getInputStream(o, 0);
        return new RawView2(contentType, in, (int) o.len());
    }

    /**
     * 将传入的数据写入响应
     * 
     * @param mimeType
     *            写回响应的 contentType，默认 text/plain
     * @param name
     *            写回响应的 Content-Disposition，默认不填写
     * @param data
     *            数据
     * @param resp
     *            响应对象
     * @throws IOException
     */
    @POST
    @PUT
    @At("/tf")
    @Ok("void")
    @Fail("void")
    public void text_format(@Param("mime") String mimeType,
                            @Param("name") String name,
                            @Param("data") String data,
                            @Param("charset") String charset,
                            @ReqHeader("User-Agent") String ua,
                            HttpServletResponse resp) throws IOException {
        resp.setContentType(Strings.sBlank(mimeType, "text/plain"));
        if (!Strings.isBlank(name))
            WnWeb.setHttpRespHeaderContentDisposition(resp, name, ua);
        OutputStreamWriter w = null;
        try {
            if (Strings.isBlank(charset))
                charset = "UTF-8";
            w = new OutputStreamWriter(resp.getOutputStream(), charset);
            w.write(data);
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(w);
            resp.flushBuffer();
        }
    }

}
