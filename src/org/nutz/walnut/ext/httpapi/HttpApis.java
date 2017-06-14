package org.nutz.walnut.ext.httpapi;

import org.nutz.lang.util.Callback;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;

/**
 * 一些帮助函数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class HttpApis {

    public static void doApi(WnSystem sys,
                             JvmHdlContext hc,
                             final Callback<HttpApiContext> callback,
                             boolean createDirIfNoExists,
                             boolean createTmpIfNoExists) {
        // 要操作的用户域
        String myName = hc.params.get("u", sys.me.name());

        // 指定了其他用户
        if (!sys.me.name().equals(myName)) {
            // 进入内核态检查权限
            Wn.WC().security(new WnEvalLink(sys.io), new Atom() {
                public void run() {
                    // 首先确保当前的用户必须为指定用户组，或者 root 或者 op 组成员
                    if (!sys.usrService.isMemberOfGroup(sys.me, "op", "root")) {
                        throw Er.create("e.cmd.httapi.nopvg");
                    }
                    // 得到这个用户的主目录
                    WnUsr u = sys.usrService.check(myName);
                    String aph = Wn.normalizePath(u.home(), sys);
                    WnObj oHome = sys.io.check(null, aph);

                    // 执行
                    __do_api(sys, oHome, u, callback, createDirIfNoExists, createTmpIfNoExists);
                }
            });
        }
        // 采用自己的域
        else {
            // 默认采用自己的主域
            WnObj oHome = sys.getHome();

            // 执行
            __do_api(sys, oHome, sys.me, callback, createDirIfNoExists, createTmpIfNoExists);
        }
    }

    private static void __do_api(WnSystem sys,
                                 WnObj oHome,
                                 WnUsr usr,
                                 final Callback<HttpApiContext> callback,
                                 boolean createDirIfNoExists,
                                 boolean createTmpIfNoExists) {
        // 准备回调上下文
        HttpApiContext c = new HttpApiContext();
        c.usr = usr;

        // 得到 api 目录
        c.oApiHome = sys.io.fetch(oHome, ".regapi");

        // 没有目录的话，输出空
        if (null == c.oApiHome) {
            throw Er.create("e.cmd.httpapi.nohome", oHome);
        }

        // 得到 api 的目录
        c.oApiDir = sys.io.fetch(c.oApiHome, "api");

        // 检查
        if (null == c.oApiDir) {
            // 确保有
            if (createDirIfNoExists) {
                c.oApiDir = sys.io.create(c.oApiHome, "api", WnRace.DIR);
            }
            // 抛错
            else {
                throw Er.create("e.cmd.httpapi.noApiDir", c.oApiHome);
            }
        }

        // 得到 tmp 的目录
        c.oApiTmp = sys.io.createIfNoExists(c.oApiHome, "tmp", WnRace.DIR);

        // // 检查
        // if (null == c.oApiTmp) {
        // // 确保有
        // if (createTmpIfNoExists) {
        // c.oApiTmp = sys.io.create(c.oApiHome, "tmp", WnRace.DIR);
        // }
        // // 抛错
        // else {
        // throw Er.create("e.cmd.httpapi.noApiTmp", c.oApiHome);
        // }
        // }

        // 执行回调
        callback.invoke(c);
    }

    // =================================================================
    // 不许实例化
    private HttpApis() {}
}
