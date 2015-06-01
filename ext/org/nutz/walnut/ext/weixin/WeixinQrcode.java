package org.nutz.walnut.ext.weixin;

import java.io.InputStream;

import org.nutz.http.Http;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.weixin.spi.WxApi2;
import org.nutz.weixin.spi.WxResp;

public class WeixinQrcode {

    private static final String URL_QR_SHOW = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=";

    public void handle(WnSystem sys, ZParams params) {
        // 处理消息的类型
        String str = params.check("qrcode");

        // 显示二维码地址
        if ("url".equals(str)) {
            String ticket = params.check("qrticket");
            String url = URL_QR_SHOW + ticket;
            sys.out.println(url);
            return;
        }

        // 显示二维码图片内容
        if ("img".equals(str)) {
            String ticket = params.check("qrticket");
            String url = URL_QR_SHOW + ticket;
            InputStream ins = Http.get(url).getStream();
            sys.out.write(ins);
            return;
        }

        // 需要用到公众号的 API 了，先得到公众号
        String pnb = params.check("pnb");

        // 得到微信的配置目录
        WnObj wxHome = sys.io.check(null, Wn.normalizeFullPath("~/.weixin/" + pnb, sys));

        // 创建微信 API
        WxApi2 wxApi = new WnIoWeixinApi(sys.io, wxHome);

        JsonFormat df = JsonFormat.nice().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        // 临时二维码
        if ("QR_SCENE".equals(str)) {
            int qrsid = params.getInt("qrsid");
            int qrexpi = params.getInt("qrexpi");
            WxResp resp = wxApi.qrcode_create(qrsid, qrexpi);
            sys.out.println(Json.toJson(resp, df));
            return;
        }
        // 永久二维码
        if ("QR_LIMIT_SCENE".equals(str)) {
            int qrsid = params.getInt("qrsid");
            WxResp resp = wxApi.qrcode_create(qrsid, -1);
            sys.out.println(Json.toJson(resp, df));
            return;
        }
        // 永久字符串二维码
        if ("QR_LIMIT_STR_SCENE".equals(str)) {
            String qrsid = params.check("qrsid");
            WxResp resp = wxApi.qrcode_create(qrsid, -1);
            sys.out.println(Json.toJson(resp, df));
            return;
        }

        // 不能识别的指令
        throw Er.create("e.cmd.weixin.qrcode.invalid", str);
    }

}
