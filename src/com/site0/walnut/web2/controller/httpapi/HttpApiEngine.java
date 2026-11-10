package com.site0.walnut.web2.controller.httpapi;

import java.io.PrintWriter;

import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcResponse;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnRun;
import com.site0.walnut.util.Ws;

/**
 * RM-NUTZ-09：动态 API 引擎。照抄旧 {@code HttpApiModule.do_api}（75-253）的 18 步
 * 流水线（任务文档 §3.3 锚点），私有方法组拆到同包小帮助类（§3.1 分工表）。
 *
 * <p>引擎直接面向标准中立接口 {@link WnMvcRequest}/{@link WnMvcResponse}（零 javax、
 * 零 unwrap）；继承 {@link WnRun} 获得 {@code io()/auth()/conf/boxes()/exec/
 * createBoxContext/runWithHook}，由 controller 每请求 new 并用 WnRun 的 setter 装配。</p>
 *
 * <p>业务错误保持旧语义：引擎内 catch → {@code sendError(404=e.io.obj.noexists /
 * 403=e.api.forbid / 500=其余)}，{@code >=500} 还要 {@code printStackTrace(writer())}，
 * {@code sendError} 后 {@code flush()}。</p>
 */
public class HttpApiEngine extends WnRun {

    private static final Log log = Wlog.getCMD();

    /**
     * 18 步流水线（§3.3 锚点逐条对应）。
     *
     * @param usr
     *            路径绑定的操作者用户名（{@code ?} 段）
     * @param api
     *            路径绑定的 API 相对路径（{@code **} 余段）
     * @param req
     *            标准中立请求
     * @param resp
     *            标准中立响应
     */
    public void doApi(String usr, String api, WnMvcRequest req, WnMvcResponse resp) {
        final WnHttpApiContext apc = new WnHttpApiContext();
        apc.usr = usr;
        apc.api = api;
        apc.req = req;
        apc.resp = resp;
        apc.reqMeta = new NutMap();
        apc.reqQueryMap = new NutMap();

        // 小帮助类（§3.1 分工表；每请求随引擎一起 new）
        ApiObjFinder finder = new ApiObjFinder(io());
        ApiMetaFiller filler = new ApiMetaFiller(io());
        ApiWwwAuth wwwAuth = new ApiWwwAuth(io());
        ApiPreload preload = new ApiPreload(this);
        ApiExecutor executor = new ApiExecutor(this);
        ApiCache cache = new ApiCache(io(), executor);
        ApiReqObject reqObject = new ApiReqObject(io());
        ApiHistory history = new ApiHistory(io());

        // 找到用户和对应的命令
        try {
            if (log.isInfoEnabled())
                log.infof("httpAPI(%s): /%s/%s", req.getRemoteAddr(), usr, api);

            // .........................................
            // 得到用户和主目录
            apc.u = auth().checkUser(usr);
            String homePath = apc.u.getHomePath();
            apc.oHome = io().check(null, homePath);

            // .........................................
            // 找到 API 对象
            apc.oApi = finder.findApiObj(apc);

            // .........................................
            // 如果没有这个 API 文件，就 404 吧
            if (null == apc.oApi) {
                apc.respCode = 404;
                throw Er.create("e.api.nofound", api);
            }

            // .........................................
            // 如果有原来的老会话，则记录一下操作会话
            apc.oldSe = null;
            String ticket = Wn.WC().getTicket();
            if (!Ws.isBlank(ticket)) {
                apc.oldSe = this.auth().getSession(ticket);
            }

            // .........................................
            // 将当前线程切换到指定的用户
            apc.se = __switch_op_user(apc);

            // .........................................
            // 如果 API 文件声明了需要 copy 的 cookie 到线程上下文 ...
            filler.copyCookieToWcContext(apc);

            // .........................................
            // 如果是跨域的 Preflighted 请求，直接根据设定返回就是
            if ("OPTIONS".equals(req.getMethod())) {
                executor.doHttpOptions(apc);

                // 无论如何，都不需要继续创建请求对象之类的了
                return;
            }

            // .........................................
            // 填充请求元数据
            filler.fillReqMeta(apc);

            // .........................................
            // 验证 Access token
            filler.checkApiAccessToken(apc);

            // .........................................
            // 对所有的元数据进行逃逸处理
            filler.escapeReqMeta(apc);

            // .........................................
            // 自动登陆站点用户
            wwwAuth.doWwwAuth(apc);

            // .........................................
            // 预先加载数据
            preload.doPreload(apc);

            // .........................................
            // 这里处理缓存
            if (cache.tryCache(apc)) {
                return;
            }

            // .........................................
            // 检查权限
            if (null != apc.wwwSe) {
                wwwAuth.doWwwCheckPvg(apc);
            }

            // .........................................
            // 准备临时目录
            apc.oTmp = reqObject.genTmpObj(apc);

            // .........................................
            // 生成请求对象
            apc.oReq = reqObject.genReqObj(apc);

            // .........................................
            // 执行 API 文件
            // 带钩子方式的运行
            if (apc.oApi.getBoolean("run-with-hook")) {
                executor.doApiWithHook(apc);
            }
            // 不带钩子
            else {
                executor.doApi(apc);
            }

            // 执行历史记录
            history.recordHistory(apc);
        }
        catch (Exception e) {
            // .........................................
            // 根据类型，设置 HTTP 错误码
            // （Er.wrap(e)==e ⟺ e instanceof WebException，避免 import org.nutz.web）
            if (Er.wrap(e) == e) {
                String ek = Er.wrap(e).getKey();
                if (log.isWarnEnabled()) {
                    log.infof("API %s : '%s'", apc.api, e.toString());
                }
                // 有东西木有找到
                if ("e.io.obj.noexists".equals(ek)) {
                    apc.respCode = 404;
                }
                // 没有权限
                else if ("e.api.forbid".equals(ek)) {
                    apc.respCode = 403;
                }
            }
            // .........................................
            // 不是 4xx 的话，默认设置 500，并且打印的
            if (apc.respCode < 400) {
                apc.respCode = 500;
            }
            if (apc.respCode >= 500) {
                if (log.isWarnEnabled()) {
                    log.warn("Fail to handle API", e);
                }
                e.printStackTrace(new PrintWriter(resp.writer()));
            }
            // .........................................
            resp.sendError(apc.respCode);
            resp.flush();
            return;
        }
        // 确保退出登录
        finally {
            // 将请求的对象设置一下清除标志（默认缓存 1 分钟)
            if (null != apc.oReq) {
                long dftDu = this.getConfig().getLong("http-api-tmp-duration", 60000L);
                // 如果有站点的话，则看看站点的默认会话设定
                if (null != apc.oWWW) {
                    int duInS = apc.oWWW.getInt("api_req_du", -1);
                    if (duInS >= 0) {
                        dftDu = duInS * 1000L;
                    }
                }
                long tmpDu = apc.oApi.getLong("http-tmp-duraion", dftDu);
                // 如果时间短于 1 秒，就直接删除了
                if (tmpDu < 1000) {
                    io().delete(apc.oReq);
                }
                // 否则，让清理进程删除
                else {
                    long expi = Wn.now() + tmpDu;
                    apc.oReq.expireTime(expi);
                    io().set(apc.oReq, "^(expi)$");
                }
            }

            // 注销会话
            if (null != apc.se) {
                auth().removeSession(apc.se);
            }
            // 与旧行 `apc.wc.setSession(null)` 的唯一差异是加了 null 防守：
            // wc 仅在 __switch_op_user 之后才非空；404 等早退路径旧代码此处会 NPE
            // （响应已提交，旧 Nutz @Fail("void") 吞掉）。防守后可观察行为与旧一致，
            // 只是不再产生无意义的 NPE 日志。
            if (null != apc.wc) {
                apc.wc.setSession(null);
            }
        }

    }

    private WnSession __switch_op_user(final WnHttpApiContext apc) {
        apc.wc = Wn.WC();
        int du = auth().getSessionDuration(false);
        WnSession se = auth().createSession(apc.u, Wn.SET_LOGIN_API, du);
        apc.wc.setSession(se);
        return se;
    }

    /**
     * 暴露 {@link WnRun#createBoxContext(WnSession)}（protected）给同包的
     * {@link ApiExecutor} 使用。
     */
    public WnBoxContext newBoxContext(WnSession se) {
        return createBoxContext(se);
    }

}
