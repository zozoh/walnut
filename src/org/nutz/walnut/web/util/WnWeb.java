package org.nutz.walnut.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;

public abstract class WnWeb {

    /**
     * 根据对象，判断是否应该返回用户的浏览器下载目标。
     * 
     * @param o
     *            对象
     * @param ua
     *            客户端的 UserAgent 字段
     * 
     * @param downMedia
     *            是否下载媒体
     * 
     * @return 浏览器的 User-Agent 响应端段。null 表示这种对象不要根据 User-Agent 生成下载目标段
     */
    public static String autoUserAgent(WnObj o, String ua, boolean downMedia) {
        String mime = o.mime();

        if (!Strings.isBlank(mime)) {
            // 媒体看开关
            if (mime.startsWith("image/")
                || mime.startsWith("video/")
                || mime.startsWith("audio/")) {
                return downMedia ? ua : null;
            }

            // 文本不下载
            if (mime.startsWith("text/"))
                return null;

            // JS / JSON 不下载
            if (mime.contains("javascript") || mime.contains("json")) {
                return null;
            }
        }

        // 其他
        return ua;
    }

    public static void setHttpRespHeaderContentDisposition(HttpServletResponse resp,
                                                           String fnm,
                                                           String ua) {
        resp.setHeader("CONTENT-DISPOSITION", genHttpRespHeaderContentDisposition(fnm, ua));
    }

    public static String genHttpRespHeaderContentDisposition(String fnm, String ua) {
        try {
            // Safari 狗屎
            if (null != ua && ua.contains(" Safari/")) {
                String e_fnm = new String(fnm.getBytes("UTF-8"), "ISO8859-1");
                return "attachment; filename=\"" + e_fnm + "\"";
            }
            // 其他用标准
            else {
                String e_fnm = URLEncoder.encode(fnm, Encoding.UTF8);
                return "attachment; filename*=utf-8'zh_cn'" + e_fnm;
            }
        }
        catch (UnsupportedEncodingException e) {
            throw Lang.wrapThrow(e);
        }
    }

    // 禁止实例化
    private WnWeb() {}

}
