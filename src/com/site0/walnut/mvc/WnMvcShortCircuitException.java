package com.site0.walnut.mvc;

/**
 * 动作链短路标记（RM-NUTZ-16）：过滤器已自行写完响应（如 CORS 预检 204），
 * 抛出本异常让 {@link WnMvcChain} 静默结束当前链（不渲染失败视图、不打 warn 日志），
 * 尾部过滤器照常 finally 执行。
 * <p>
 * 本异常不是失败语义，不携带状态码；响应状态/头由抛出方负责写入。
 */
public class WnMvcShortCircuitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WnMvcShortCircuitException() {
        super("WnMvc short circuit", null, false, false);
    }

}
