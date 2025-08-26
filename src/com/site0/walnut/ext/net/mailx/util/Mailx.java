package com.site0.walnut.ext.net.mailx.util;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.mailx.bean.WnMailSecurity;
import com.site0.walnut.ext.net.mailx.bean.WnMailSign;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplX;

import org.simplejavamail.api.mailer.config.Pkcs12Config;
import org.simplejavamail.api.mailer.config.Pkcs12Config.Pkcs12ConfigBuilder;

public abstract class Mailx {

    private static final Log log = Wlog.getCMD();

    public static void LOG(WnSystem sys,
                           boolean showDebug,
                           String fmt,
                           Object... args) {
        String msg = String.format(fmt, args);
        if (showDebug) {
            sys.out.println(msg);

        }
        if (log.isInfoEnabled()) {
            log.info(msg);
        }
    }

    /**
     * 解析 HTTP 或者 Email 里面的 contnentType 符串 ，譬如
     * <code>Content-Type: text/html;charset="gb18030" </code> 将被返回 "text/html"
     * 以及将 charset=gb18030 添加到返回属性集里
     * 
     * @param contentType
     *            内容类型字符串
     * @param attrs
     *            返回属性集合
     * @return 一个标准的 contentType 字符串
     */
    public static String evalContentType(String contentType, NutBean attrs) {
        if (null == contentType) {
            return null;
        }
        String[] ss = Ws.splitIgnoreBlank(contentType, ";");

        // 之后的看属性
        if (null != attrs) {
            for (int i = 0; i < ss.length; i++) {
                String s = ss[i];
                int pos = s.indexOf('=');
                if (pos > 0) {
                    String name = s.substring(0, pos).trim();
                    String value = s.substring(pos + 1);
                    if (Ws.isQuoteBy(value, '"', '"')) {
                        value = Json.fromJson(value).toString();
                    }
                    attrs.put(name, value);
                }
            }
        }
        // 第一个就是 contentType

        return ss[0];
    }

    public static Pkcs12Config createPkcs12Config(WnSystem sys,
                                                  WnMailSecurity secu) {
        return createPkcs12Config(sys.io, sys.session.getEnv(), secu);
    }

    public static Pkcs12Config createPkcs12Config(WnIo io,
                                                  NutBean sessoionVars,
                                                  WnMailSecurity secu) {
        WnMailSign sign = secu.getSign();
        Pkcs12ConfigBuilder bu = Pkcs12Config.builder();
        Pkcs12Config pkcs12;

        String storePath = sign.getStorePath();
        String storePasswd = sign.getStorePassword();
        String keyAlias = sign.getKeyAlias();
        String keyPassword = Ws.sBlank(sign.getKeyPassword(), "");

        WnObj oStore = Wn.checkObj(io, sessoionVars, storePath);
        byte[] bs = io.readBytes(oStore);

        pkcs12 = bu.pkcs12Store(bs)
            .storePassword(storePasswd)
            .keyAlias(keyAlias)
            .keyPassword(keyPassword)
            .build();
        return pkcs12;
    }

    public static void joinHeaders(StringBuilder sb,
                                   NutMap map,
                                   String prefix) {
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            sb.append("\n")
                .append(prefix)
                .append(" - ")
                .append(key)
                .append(": ");
            if (null != val) {
                if (val instanceof CharSequence) {
                    sb.append(val.toString());
                } else {
                    sb.append(Json.toJson(val));
                }
            }
        }
    }

    public static void putAll(NutBean bean, NutMap map, String prefix) {
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            String k = null == prefix ? key : prefix + key;
            bean.put(k, val);
        }
    }

    public static WnTmplX getTmpl(ZParams params, String key) {
        String str = params.getString(key);
        WnTmplX re = null;
        if (!Ws.isBlank(str)) {
            re = WnTmplX.parse(str);
        }
        return re;
    }
}
