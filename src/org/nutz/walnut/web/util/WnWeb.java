package org.nutz.walnut.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;

public abstract class WnWeb {

    public static void setHttpRespHeaderContentDisposition(HttpServletResponse resp,
                                                           String fnm,
                                                           String ua) {
        try {
            // Safari 狗屎
            if (ua.contains(" Safari/")) {
                String e_fnm = new String(fnm.getBytes("UTF-8"), "ISO8859-1");
                resp.setHeader("CONTENT-DISPOSITION", "attachment; filename=\"" + e_fnm + "\"");
            }
            // 其他用标准
            else {
                String e_fnm = URLEncoder.encode(fnm, Encoding.UTF8);
                resp.setHeader("CONTENT-DISPOSITION", "attachment; filename*=utf-8'zh_cn'" + e_fnm);
            }
        }
        catch (UnsupportedEncodingException e) {
            throw Lang.wrapThrow(e);
        }
    }

    private WnWeb() {}

}
