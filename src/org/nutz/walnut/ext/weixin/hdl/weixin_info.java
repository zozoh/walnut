package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.weixin.WxConf;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 显示全部配置信息
 * weixin xxx info
 * 
 * # 显示被正则表达式约束的配置信息
 * weixin xxx info "^pay_.+$"
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(c|n|q)$")
public class weixin_info implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 读取微信配置信息
        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);
        WxConf conf = wxApi.getConfig();

        String regex = hc.params.vals.length > 0 ? hc.params.vals[0] : null;
        NutMap map = Lang.obj2nutmap(conf);
        String[] keys = map.keySet().toArray(new String[map.size()]);
        for (String key : keys) {
            if (null != regex && !key.matches(regex)) {
                map.remove(key);
            }
        }

        sys.out.println(Json.toJson(map, hc.jfmt));
    }

}
