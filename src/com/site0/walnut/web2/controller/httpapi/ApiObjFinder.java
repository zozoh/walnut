package com.site0.walnut.web2.controller.httpapi;

import java.util.LinkedList;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Ws;

/**
 * RM-NUTZ-09：照抄旧 {@code 旧 HTTP API 模块.__find_api_obj}（407-461）：
 * {@code .regapi/api} 树逐段查找 + {@code _ANY} 回退。
 */
public class ApiObjFinder {

    private final WnIo io;

    public ApiObjFinder(WnIo io) {
        this.io = io;
    }

    public WnObj findApiObj(WnHttpApiContext apc) {
        // 准备返回值
        WnObj oApi = null;

        // 首先准备路径参数
        apc.args = new LinkedList<>();
        apc.params = new NutMap();

        // 得到 api 的主目录，分解要获取的路径
        apc.oApiHome = io.fetch(apc.oHome, ".regapi/api");
        String[] phs = Ws.splitIgnoreBlank(apc.api, "/");

        // 依次取得
        for (int i = 0; i < phs.length; i++) {
            String ph = phs[i];

            // 直接找一下看看有没有
            oApi = io.fetch(apc.oApiHome, ph);
            if (null != oApi) {
                apc.oApiHome = oApi;
                continue;
            }

            // 嗯，那就找 _ANY 咯
            oApi = io.fetch(apc.oApiHome, "_ANY");

            // 没有的话就是 null 咯
            if (null == oApi)
                return null;

            apc.oApiHome = oApi;

            // 目录的话，继续
            if (oApi.isDIR()) {
                apc.args.add(ph);
                String pnm = oApi.getString("api-param-name");
                if (!Ws.isBlank(pnm)) {
                    apc.params.put(pnm, ph);
                }
            }
            // 文件，表示的是 * 嘛，那就不要继续了
            else {
                String arg = Ws.join(phs, "/", i, phs.length - i);
                apc.args.add(arg);
                String pnm = oApi.getString("api-param-name");
                if (!Ws.isBlank(pnm)) {
                    apc.params.put(pnm, arg);
                }
                break;
            }
        }

        // 搞定，收工
        return oApi;
    }

}
