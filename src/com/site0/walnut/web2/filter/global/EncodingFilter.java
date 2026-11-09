package com.site0.walnut.web2.filter.global;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilter;

/**
 * 全局编码过滤器（head）：对照旧链的 EncodingProcessor。
 * <p>
 * 默认 UTF-8；编码名写入 {@link WnMvcContext#attrs()} 的
 * <code>wn.mvc.encoding</code>，供适配层/视图使用。若响应已预设 Content-Type 且
 * 不含 charset，则补上 <code>;charset=...</code>。
 */
public class EncodingFilter implements WnMvcFilter {

    private final String encoding;

    public EncodingFilter(String encoding) {
        this.encoding = (null == encoding || encoding.trim().isEmpty())
            ? "UTF-8"
            : encoding.trim();
    }

    public String encoding() {
        return encoding;
    }

    @Override
    public void doFilter(WnMvcContext ctx) {
        ctx.attrs().put("wn.mvc.encoding", encoding);
        String ct = ctx.response().header("Content-Type");
        if (null != ct && !ct.toLowerCase().contains("charset")) {
            ctx.response().contentType(ct + ";charset=" + encoding);
        }
    }

}
