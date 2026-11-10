/**
 * Web 过滤器（WnMvcFilter）。
 * <p>
 * 职责：在进入 controller 之前完成会话/安全/编码/清理等横切逻辑。
 * <p>
 * <b>本任务（RM-NUTZ-04）只建包占位，不写任何实现类</b>；真实 filter 由
 * RM-NUTZ-05（HTTP 适配层与动作链）及后续任务填充。
 * <p>
 * 边界：本包不得直接 import {@code org.eclipse.jetty.*} / {@code jakarta.servlet.*}，
 * 容器相关代码集中放在 {@code com.site0.walnut.web2.jetty}。
 */
package com.site0.walnut.web2.filter;
