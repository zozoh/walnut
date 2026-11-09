package com.site0.walnut.mvc;

import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.impl.box.JvmExecutorFactory;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.web.WnConfig;

/**
 * 控制器公共基类：对照旧 <code>AbstractWnModule</code>/<code>WnRun</code>，提供
 * mimes / 服务访问 / 常用 helper。新 controller 继承本类，<b>不继承旧基类</b>。
 *
 * <h2>与旧基类的区别</h2>
 * <p>旧 <code>AbstractWnModule</code> 通过 Nutz <code>@Inject</code> 把服务注入为实例字段，
 * 并依赖 <code>HttpServletRequest</code>。本基类<b>不持有服务字段、不依赖 Servlet API</b>：
 * 服务能力通过每请求的 {@link WnMvcContext} 按需取用（见各 helper 的 Javadoc）。</p>
 *
 * <h2>线程模型</h2>
 * <p>子类实例由 {@link WnMvcFactory} <b>每请求 born 一个新实例</b>（即用即弃）；本基类不声明
 * 任何可变实例字段，实例之间零共享。子类若需常量（如默认值）必须是 <code>static final</code>
 * 或不可变对象。</p>
 */
public abstract class AbstractWnController implements WnMvcController {

    // ======================================================================
    // 服务访问 helper（本任务仅定签名，方法体 noImplement；JETTY-04/05 实现）
    // ======================================================================

    /**
     * 服务集合（能力门）。来自 <code>ctx.services()</code>。
     */
    protected WnServiceHolder services(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * IO 能力。来自 <code>ctx.io()</code>。
     */
    protected WnIo io(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * MIME 映射。来自 <code>ctx.mimes()</code>。
     */
    protected MimeMap mimes(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * 登录/账户 API。来自 <code>ctx.services().getLoginApi()</code>。
     */
    protected WnLoginApi auth(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * 沙箱服务。来自 <code>ctx.services().getBoxApi()</code>。
     */
    protected WnBoxService boxes(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * 钩子服务。来自 <code>ctx.services().getHookApi()</code>。
     */
    protected WnHookService hooks(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * 命令执行器工厂。来自 <code>ctx.services()</code>（JETTY-04 补充）。
     */
    protected JvmExecutorFactory jef(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * 配置对象。来自 <code>ctx.services().getConfig()</code>。
     */
    protected WnConfig conf(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * 定时任务 API。来自 <code>ctx.services().getTaskApi()</code>。
     */
    protected WnSysTaskApi tasks(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * 计划任务 API。来自 <code>ctx.services().getScheduleApi()</code>。
     */
    protected WnSysScheduleApi schedules(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    /**
     * Cron API。来自 <code>ctx.services().getCronApi()</code>。
     */
    protected WnSysCronApi crons(WnMvcContext ctx) {
        throw Wlang.noImplement();
    }

    // ======================================================================
    // 常用 helper（对照 AbstractWnModule；本任务仅定签名，方法体 noImplement）
    // ======================================================================

    /**
     * 查找指定 App 的 home 对象（沿 <code>APP_PATH</code> 各段）。
     *
     * @param ctx
     *            每请求上下文
     * @param appName
     *            App 名
     * @return home 对象，不存在时返回 <code>null</code>
     */
    protected WnObj _find_app_home(WnMvcContext ctx, String appName) {
        throw Wlang.noImplement();
    }

    /**
     * 查找指定 App 的 home 对象，不存在则抛 <code>e.app.noexists</code>。
     *
     * @param ctx
     *            每请求上下文
     * @param appName
     *            App 名
     * @return home 对象
     */
    protected WnObj _check_app_home(WnMvcContext ctx, String appName) {
        throw Wlang.noImplement();
    }

}
