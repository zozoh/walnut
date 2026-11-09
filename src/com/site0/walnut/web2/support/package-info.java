/**
 * 启动流程的辅助工具（按需）。
 * <p>
 * 本任务（JETTY-04）把启动所需的少量纯工具直接放在使用它的类里（例如
 * {@code WnWebBoot} 的"关键目录自检"），因此本包暂时没有实现类；后续任务
 * 确实需要复用的辅助函数再放进来。
 * <p>
 * 边界：不得放任何工厂/服务入口（禁止单例与静态服务入口），不得 import
 * {@code org.nutz.mvc/ioc/web}。
 */
package com.site0.walnut.web2.support;
