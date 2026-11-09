/**
 * Web 视图（WnMvcView）。
 * <p>
 * 职责：把 controller 的返回对象序列化为 HTTP 响应（JSON/文本/重定向/错误页等）。
 * <p>
 * <b>本任务（JETTY-04）只建包占位，不写任何实现类</b>；真实 view 由
 * JETTY-05~09 按需填充。
 * <p>
 * 边界：本包不得直接 import {@code org.eclipse.jetty.*} / {@code javax.servlet.*}，
 * 容器相关代码集中放在 {@code com.site0.walnut.web2.jetty}。
 */
package com.site0.walnut.web2.view;
