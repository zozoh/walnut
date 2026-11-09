package com.site0.walnut.mvc;

/**
 * 一个响应 Cookie 的不可变值对象（含 <code>Set-Cookie</code> 的常用属性）。
 *
 * <p>本类是「必要的不可变数据类」，仅承载数据、无行为逻辑；用于 {@link WnMvcResponse#cookie(WnMvcCookie)}
 * 设置响应 Cookie。SameSite 见 {@link SameSite}。</p>
 */
public class WnMvcCookie {

    /** Cookie 的 SameSite 属性。 */
    public enum SameSite {
        /** 不携带同站限制（跨站请求也携带）。 */
        NONE,
        /** 宽松：顶层导航携带。 */
        LAX,
        /** 严格：仅同站请求携带。 */
        STRICT
    }

    private final String name;

    private final String value;

    private final String path;

    private final String domain;

    private final long maxAge;

    private final boolean secure;

    private final boolean httpOnly;

    private final SameSite sameSite;

    public WnMvcCookie(String name,
                       String value,
                       String path,
                       String domain,
                       long maxAge,
                       boolean secure,
                       boolean httpOnly,
                       SameSite sameSite) {
        this.name = name;
        this.value = value;
        this.path = path;
        this.domain = domain;
        this.maxAge = maxAge;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.sameSite = sameSite;
    }

    /** 一个会话期 Cookie（<code>Path=/</code>）。 */
    public static WnMvcCookie of(String name, String value) {
        return new WnMvcCookie(name, value, "/", null, -1, false, false, null);
    }

    /** 一个删除 Cookie（<code>maxAge=0</code>，<code>Path=/</code>）。 */
    public static WnMvcCookie remove(String name) {
        return new WnMvcCookie(name, "", "/", null, 0, false, false, null);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public String path() {
        return path;
    }

    public String domain() {
        return domain;
    }

    /** max-age 秒；<code>-1</code> 表示会话期，<code>0</code> 表示删除。 */
    public long maxAge() {
        return maxAge;
    }

    public boolean secure() {
        return secure;
    }

    public boolean httpOnly() {
        return httpOnly;
    }

    public SameSite sameSite() {
        return sameSite;
    }

}
