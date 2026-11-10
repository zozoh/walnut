/**
 * 装配帮助类集合（RM-NUTZ-04）。
 * <p>
 * 每个类负责一组服务的"创建 + 配置 + 注册"，把原来超长的
 * {@code WnServiceFactory} 构造函数按服务域拆开；工厂只保留三个阶段
 * （构造器 / {@code initStores()} / {@code initServices()}）的编排。
 * <p>
 * 约定：
 * <ul>
 * <li>全部是<b>静态、无状态</b>的直接调用，仅装配期使用；它们<b>不是运行期服务入口</b>，
 * 业务代码要从 {@code WnServiceFactory} 的类型化 getter 取服务。</li>
 * <li>依赖顺序即调用顺序：后注册的类可用 {@code factory.fetch(name, Type.class)}
 * 取前面已注册的 bean。</li>
 * <li>不 import {@code org.nutz.mvc/ioc/web}。</li>
 * </ul>
 */
package com.site0.walnut.web2.service.register;
