package org.nutz.walnut.ext.net.util;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import org.nutz.walnut.util.Ws;

public class WnNet {

    private static final Map<String, NutMap> BROWSER_HEADERS = new HashMap<>();

    static {
        NutMap CHROME_HEADERS = new NutMap();
        CHROME_HEADERS.put("Proxy-Connection", "keep-alive");
        CHROME_HEADERS.put("Accept", "*/*");
        CHROME_HEADERS.put("User-Agent",
                           "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36");
        CHROME_HEADERS.put("Accept-Encoding", "gzip, deflate");
        CHROME_HEADERS.put("Accept-Language", "zh-CN,zh;q=0.9");
        BROWSER_HEADERS.put("chrome", CHROME_HEADERS);
    }

    public static NutBean getBrowserHeader(String name) {
        return BROWSER_HEADERS.get(name);
    }

    private static final Pattern P_IPV4 = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static final Pattern P_IPV6_STD = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

    private static final Pattern P_IPV6_HEX_COMPRESSED = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

    public static boolean isIPv4Address(final String input) {
        return P_IPV4.matcher(input).matches();
    }

    public static boolean isIPv6StdAddress(final String input) {
        return P_IPV6_STD.matcher(input).matches();
    }

    public static boolean isIPv6HexCompressedAddress(final String input) {
        return P_IPV6_HEX_COMPRESSED.matcher(input).matches();
    }

    public static boolean isIPv6Address(final String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
    }

    public static void joinQuery(NutBean q, String input, boolean decodeValue) {
        // 防守
        if (Ws.isBlank(input)) {
            return;
        }
        // JSON 模式
        if (Ws.isQuoteBy(input, '{', '}')) {
            NutMap map = Json.fromJson(NutMap.class, input);
            q.putAll(map);
        }
        // 普通字符串模式
        else {
            putQueryString(q, input, decodeValue);
        }
    }

    public static NutMap parseQuery(String input, boolean decodeValue) {
        NutMap re = new NutMap();
        joinQuery(re, input, decodeValue);
        return re;
    }

    public static void putQueryString(NutBean map, String input, boolean decodeValue) {
        if (!Ws.isBlank(input)) {
            String[] ss = Ws.splitIgnoreBlank(input, "&");
            for (String s : ss) {
                int pos = s.indexOf('=');
                if (pos > 0) {
                    String k = s.substring(0, pos);
                    String v = s.substring(pos + 1);
                    if (decodeValue) {
                        v = URLDecoder.decode(v, Encoding.CHARSET_UTF8);
                    }
                    map.put(k, v);
                } else {
                    map.put(s, null);
                }
            }
        }
    }
}
