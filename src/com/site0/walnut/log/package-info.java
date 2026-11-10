/**
 * Walnut 自有日志封装（RM-NUTZ-11）。
 * <p>
 * 目标：RM-NUTZ-12 把全库 {@code org.nutz.log.Log/Logs} 替换为
 * {@link com.site0.walnut.log.WnLog}/{@link com.site0.walnut.log.WnLogs}，
 * 为 RM-NUTZ-14 删除 {@code org.nutz} 做准备。
 * <p>
 * 本包只做接口/工厂与 log4j2 适配，不做日志采集、格式、分级策略改造；
 * 现有 {@code com.site0.walnut.util.Wlog} 保留为薄适配入口。
 */
package com.site0.walnut.log;
