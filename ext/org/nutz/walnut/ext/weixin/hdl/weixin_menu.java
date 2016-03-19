package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.weixin.spi.WxApi2;
import org.nutz.weixin.spi.WxResp;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 获取
 * weixin xxx menu -get
 * 
 * # 删除
 * weixin xxx menu -del
 * 
 * # 设置
 * weixin xxx menu -set id:xxx
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(get|del|c|n|q)$")
public class weixin_menu implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 创建微信 API
        WxApi2 wxApi = WxUtil.genWxApi(sys, hc);

        WxResp resp;
        // 删除菜单
        if (hc.params.is("del")) {
            resp = wxApi.menu_delete();
        }
        // 获取菜单
        else if (hc.params.is("get")) {
            resp = wxApi.menu_get();
        }
        // 创建菜单
        else {
            String str = hc.params.get("set");
            String json;
            // 从管道读取
            if (Strings.isBlank(str) && sys.pipeId > 0) {
                json = sys.in.readAll();
            }
            // 从对象读取
            else if (!Strings.isBlank(str)) {
                WnObj o = Wn.checkObj(sys, str);
                json = sys.io.readText(o);
            }
            // 否则肯定是那里不对
            else {
                throw Er.create("e.cmd.weixin.menu.noinput");
            }

            NutMap map = Json.fromJson(NutMap.class, json);
            resp = wxApi.menu_create(map);
        }

        // 输出
        JsonFormat df = hc.jfmt.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sys.out.println(Json.toJson(resp, df));
    }

}
