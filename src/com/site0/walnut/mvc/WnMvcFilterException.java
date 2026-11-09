package com.site0.walnut.mvc;

/**
 * 过滤器拦截失败时抛出的<b>专有异常</b>。
 *
 * <p>{@link WnMvcFilter#doFilter(WnMvcContext)} <b>不抛出本异常即视为通过</b>；抛出本异常表示
 * 请求未通过该过滤器，由 {@link WnMvcChain} 捕获后记错误日志、按 {@link #getStatus()} 渲染失败
 * 视图并中断（fail-closed，不进入 controller）。</p>
 *
 * <p>本类是「必要的异常类型」：只承载状态码与错误码，无行为逻辑。</p>
 */
public class WnMvcFilterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** HTTP 状态码（如 401/403/404/500），供失败视图使用。 */
    private final int status;

    /** 业务错误码（如 <code>e.se.noexists</code>），可为 <code>null</code>。 */
    private final String errCode;

    public WnMvcFilterException(int status, String message) {
        this(status, null, message, null);
    }

    public WnMvcFilterException(int status, String errCode, String message) {
        this(status, errCode, message, null);
    }

    public WnMvcFilterException(int status,
                                String errCode,
                                String message,
                                Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errCode = errCode;
    }

    /**
     * HTTP 状态码。
     */
    public int getStatus() {
        return status;
    }

    /**
     * 业务错误码，可能为 <code>null</code>。
     */
    public String getErrCode() {
        return errCode;
    }

}
