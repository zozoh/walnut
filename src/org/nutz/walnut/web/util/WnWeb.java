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
     * @deprecated 这个判断比较凌乱，以后应该用 {@link #autoUserAgent(WnObj, String, String)}
     *             代替
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

    /**
     * 根据对象，判断是否应该返回用户的浏览器下载目标。
     * 
     * @param o
     *            对象
     * @param ua
     *            客户端的 UserAgent 字段
     * 
     * @param mode
     *            下载模式
     *            <ul>
     *            <li><code>auto</code> : 自动根据对象 mime判断
     *            <li><code>force</code> : 强制下载
     *            <li><code>raw</code> : 直接输出内容
     *            <li>null : 相当于 <code>auto</code>
     *            </ul>
     * 
     * @return 浏览器的 User-Agent 响应端段。null 表示这种对象不要根据 User-Agent 生成下载目标段
     */
    public static String autoUserAgent(WnObj o, String ua, String mode) {
        WnDownloadMode md = WnDownloadMode.auto;
        // 解析模式
        if (!Strings.isBlank(mode)) {
            try {
                md = WnDownloadMode.valueOf(mode);
            }
            catch (Exception e) {
                // 这里如果随便给的 mode，强制按照 auto 来算
            }
        }
        // 返回调用
        return autoUserAgent(o, ua, md);
    }

    public static String autoUserAgent(WnObj o, String ua, WnDownloadMode mode) {
        // 未指定模式，相当于自动
        if (null == mode) {
            mode = WnDownloadMode.auto;
        }

        // 直接输出内容
        if (WnDownloadMode.raw == mode) {
            return null;
        }

        // 自动根据类型判断
        if (WnDownloadMode.auto == mode) {
            String mime = o.mime();

            if (!Strings.isBlank(mime)) {
                // 文本/JS/JSON 不下载
                if (mime.startsWith("text/")
                    || mime.contains("javascript")
                    || mime.contains("json")) {
                    return null;
                }
            }
        }

        // 其他统统下载
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
