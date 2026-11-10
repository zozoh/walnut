/**
 * Web 控制器（WnMvcController）。
 * <p>
 * 职责：把 HTTP 请求转换成对 walnut 服务层的调用，并产出视图/响应。
 * <p>
 * <b>本任务（RM-NUTZ-04）只建包占位，不写任何实现类</b>；真实 controller 由
 * RM-NUTZ-06~09 按模块（Usr/App/Obj/Api）逐个填充。
 * <p>
 * 边界：本包不得直接 import {@code org.eclipse.jetty.*} / {@code javax.servlet.*}，
 * 容器相关代码集中放在 {@code com.site0.walnut.web2.jetty}。
 */
package com.site0.walnut.web2.controller;
