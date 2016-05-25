package org.nutz.walnut.ext.weixin.hdl;

import java.io.InputStream;

import org.nutz.http.Http;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.weixin.spi.WxApi2;
import org.nutz.weixin.spi.WxResp;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 显示二维码地址
 * weixin xxx qrcode url -qrticket xxxxxxx
 * 
 * # 显示二维码图片内容
 * weixin xxx qrcode img -qrticket xxxxxxx
 * 
 * # 临时二维码
 * # qrsid 整数，场景 ID
 * # qrexpi 过期秒数
 * weixin xxx qrcode QR_SCENE -qrsid 2 -qrexpi 3600
 * 
 * # 永久二维码
 * # qrsid 整数，场景 ID
 * weixin xxx qrcode QR_LIMIT_SCENE -qrsid 2
 * 
 * # 永久字符串二维码
 * # qrsid 字符串，场景 ID
 * weixin xxx qrcode QR_LIMIT_STR_SCENE -qrsid "hahaID"
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(get|del|c|n|q)$")
public class weixin_qrcode implements JvmHdl {

    private static final String URL_QR_SHOW = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 处理消息的类型
        String str = hc.params.vals[0];

        // 显示二维码地址
        if ("url".equals(str)) {
            String ticket = hc.params.check("qrticket");
            String url = URL_QR_SHOW + ticket;
            sys.out.println(url);
            return;
        }

        // 显示二维码图片内容
        if ("img".equals(str)) {
            String ticket = hc.params.check("qrticket");
            String url = URL_QR_SHOW + ticket;
            InputStream ins = Http.get(url).getStream();
            sys.out.write(ins);
            return;
        }

        // 创建微信 API
        WxApi2 wxApi = WxUtil.genWxApi(sys, hc);

        JsonFormat df = hc.jfmt.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        // 临时二维码
        if ("QR_SCENE".equals(str)) {
            int qrsid = 0;
            if ("0".equals(hc.params.get("qrsid"))) {
                WnObj tmp = sys.io.createIfNoExists(hc.oHome, "scene_seq", WnRace.FILE);
                String key = "weixin_scene_seq";
                qrsid = tmp.getInt("weixin_scene_seq", 0);
                if (qrsid == 0) {
                    tmp.put("weixin_scene_seq", 100000); // 自增的从10w开始
                    sys.io.set(tmp, "weixin_scene_seq");
                }
                qrsid = sys.io.inc(tmp.id(), key, 1, true);
            } else {
                qrsid = hc.params.getInt("qrsid");
            }
            int qrexpi = hc.params.getInt("qrexpi", 3600);
            WxResp resp = wxApi.qrcode_create(qrsid, qrexpi);
            
            long expire_time = System.currentTimeMillis() + resp.getInt("expire_seconds", 0)*1000 - 15*1000;
            resp.setv("scene_id", qrsid);
            resp.setv("scene_exp", expire_time);
            sys.out.println(Json.toJson(resp, df));
            WnObj tmp = sys.io.createIfNoExists(hc.oHome, "scene/"+qrsid, WnRace.FILE);
            // 从流中读取cmd文本,然后写入对应的scene
            String cmd = Cmds.getParamOrPipe(sys, hc.params, "cmd", true);
            if (!Strings.isBlank(cmd)) {
                sys.io.writeText(tmp, cmd);
            }
            NutMap meta = new NutMap();
            meta.put("weixin_scene_ticket", resp.get("ticket"));
            meta.put("weixin_scene_url", resp.get("url"));
            meta.put("weixin_scene_exp", expire_time);
            sys.io.setBy(tmp.id(), meta, false);
            return;
        }
        // 永久二维码
        if ("QR_LIMIT_SCENE".equals(str)) {
            int qrsid = hc.params.getInt("qrsid");
            WxResp resp = wxApi.qrcode_create(qrsid, -1);
            sys.out.println(Json.toJson(resp, df));
            return;
        }
        // 永久字符串二维码
        if ("QR_LIMIT_STR_SCENE".equals(str)) {
            String qrsid = hc.params.check("qrsid");
            WxResp resp = wxApi.qrcode_create(qrsid, -1);
            sys.out.println(Json.toJson(resp, df));
            return;
        }

        // 不能识别的指令
        throw Er.create("e.cmd.weixin.qrcode.invalid", str);

    }

}
