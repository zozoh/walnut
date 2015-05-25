package org.nutz.walnut.ext.weixin;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.weixin.bean.WxArticle;
import org.nutz.weixin.bean.WxInMsg;
import org.nutz.weixin.bean.WxOutMsg;
import org.nutz.weixin.util.Wxs;

public class cmd_weixin extends JvmExecutor {

    private static final Log log = Logs.get();

    private WeixinIn wxin;

    private WeixinMenu wxmenu;

    public cmd_weixin() {
        wxin = new WeixinIn();
        wxmenu = new WeixinMenu();
    }

    @Override
    public void exec(final WnSystem sys, String[] args) {
        // 分析参数
        ZParams params = ZParams.parse(args, null);

        // 处理输入
        if (params.has("in")) {
            String str = params.get("in");
            WnObj o = Wn.checkObj(sys, str);
            wxin.handle(sys, o);
        }
        // 处理菜单
        else if (params.has("menu")) {
            String pnb = params.check("pnb");
            try {
                wxmenu.handle(sys, params.get("menu"), pnb);
            }
            catch (Exception e) {
                log.warn("!!!", e);
            }
        }
        // 输出微信的响应消息
        else if (params.has("out")) {
            String out = params.get("out");
            WxOutMsg om = null;

            // 一个 JSON
            if (Strings.isQuoteBy(out, '{', '}')) {
                om = Json.fromJson(WxOutMsg.class, out);
            }
            // 一个简单的文本
            else if (out.startsWith("text:")) {
                om = Wxs.respText(null, out.substring("text:".length()));
            }
            // 一篇简单的文章
            else if (out.startsWith("article:")) {
                String[] ss = Strings.splitIgnoreBlank(out.substring("article:".length()), ";;");
                WxArticle arti = new WxArticle();
                arti.setTitle(ss[0]);
                if (ss.length > 1)
                    arti.setDescription(ss[1]);
                if (ss.length > 2)
                    arti.setUrl(ss[2]);
                om = Wxs.respNews(null, arti);
            }
            // 试图从一个对象里读取文本内容
            else {
                WnObj oMsg = Wn.checkObj(sys, out);
                om = sys.io.readJson(oMsg, WxOutMsg.class);
            }
            // 如果指明了输入源，则视图覆盖 from/toUserName
            String inmsg = params.get("inmsg");
            WnObj oi = null;
            if (!Strings.isBlank(inmsg)) {
                oi = Wn.checkObj(sys, inmsg);
                WxInMsg im = WxUtil.getFromObj(oi);
                Wxs.fix(im, om);
            }
            // 写入标准输出
            String xml = Wxs.asXml(om);
            sys.out.println(xml);
        }
        // 无法处理
        else {
            throw Er.create("e.cmd.weixin.invalid", args);
        }

    }

}
