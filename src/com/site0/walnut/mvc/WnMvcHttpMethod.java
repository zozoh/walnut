package com.site0.walnut.mvc;

/**
 * WnMvc 路由可声明的 HTTP 方法（枚举）。
 *
 * <p>路由注册只走 {@link WnMvcMapping#route(WnMvcHttpMethod[], String, Class, WnMvcFilter[], WnMvcFilter[])}，
 * 方法约束用本枚举<b>显式数组</b>声明：单方法用单元素数组；{@link #ANY} 表示不限定方法。</p>
 */
public enum WnMvcHttpMethod {

    /** GET */
    GET,

    /** POST */
    POST,

    /** PUT */
    PUT,

    /** DELETE */
    DELETE,

    /** PATCH */
    PATCH,

    /** HEAD */
    HEAD,

    /** OPTIONS */
    OPTIONS,

    /** 不限定方法（等价旧 @At 无方法约束），匹配任意 HTTP 方法 */
    ANY

}
