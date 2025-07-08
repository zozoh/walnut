package com.site0.walnut.web.module;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import com.site0.walnut.util.Wlang;
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
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.ext.media.qrcode.WnQrCode;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.filter.WnAsUsr;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnObjDownloadView;

/**
 * 提供未登录访客访问内容的方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@At("/gu")
@Filters(@By(type = WnAsUsr.class, args = {"guest"}))
public class GuestModule extends AbstractWnModule {

    @Inject
    protected PropertiesProxy conf;

    // 这个临时变量是在 conf 被设置时初始化的
    protected boolean enableSecurity;

    @At("/**")
    @Fail("http:404")
    public View read(String str,
                     @Param("d") String download,
                     @Param("dnm") String downloadName,
                     @ReqHeader("User-Agent") String ua,
                     @ReqHeader("If-None-Match") String etag,
                     @ReqHeader("Range") String range,
                     HttpServletRequest req,
                     HttpServletResponse resp) {
        // 获取对象
        WnObj o = Wn.checkObj(io(), str);
        // 安全性 检查
        // 确保可读，同时处理链接文件
        if (enableSecurity) {
            WnSecurity wns = Wn.WC().getSecurity();
            try {
                Wn.WC().setSecurity(new WnSecurityImpl(io(), login()));
                o = Wn.WC().whenRead(o, false);
            }
            finally {
                Wn.WC().setSecurity(wns);
            }
        } else {
            o = Wn.WC().whenRead(o, false);
        }

        // 纠正一下下载模式
        ua = WnWeb.autoUserAgent(o, ua, download);

        // 返回下载视图
        return new WnObjDownloadView(io(), o, downloadName, ua, etag, range);
    }

    @At("/qrcode")
    @Ok("void")
    @Fail("void")
    public void qrcode(@Param("d") String data,
                       @Param("s") int size,
                       @Param("f") String fmt,
                       @Param("m") int margin,
                       HttpServletResponse resp)
            throws IOException {
        // 得到二维码内容
        data = Strings.sBlank(data, "NoData");
        data = URLDecoder.decode(data, "UTF-8");

        // 默认
        size = size < 50 ? 256 : size;
        fmt = Strings.sBlank(fmt, "png");
        if ("jpg".equals(fmt)) {
            fmt = "jpeg";
        }
        margin = margin < 0 ? 1 : margin;

        // 写入 Header
        resp.setContentType("image/" + fmt);

        // 得到输出流
        OutputStream ops = resp.getOutputStream();

        try {
            // 生成二维码
            WnQrCode qr = new WnQrCode(data, margin, size, size);

            // 输出
            qr.writeTo(ops, fmt);
        }
        catch (Exception e) {
            throw Wlang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(ops);
        }
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
                            HttpServletResponse resp)
            throws IOException {
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
            throw Wlang.wrapThrow(e);
        }
        finally {
            Streams.safeClose(w);
            resp.flushBuffer();
        }
    }

    public void setConf(PropertiesProxy conf) {
        this.conf = conf;
        this.enableSecurity = conf.getBoolean("gu-security", false);
    }
}
