package org.nutz.walnut.ext.weixin;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.weixin.bean.WxInMsg;
import org.nutz.weixin.bean.WxOutMsg;
import org.nutz.weixin.util.Wxs;

public class WeixinIn {

    public void handle(WnSystem sys, WnObj o, boolean debug) {
        Object method = o.get("http-method");
        // 认证
        if ("GET".equals(method)) {
            do_GET(sys, o);
        }
        // 消息输入
        else if ("POST".equals(method)) {
            do_POST(sys, o, debug);
        }
        // 最后设置删除时间（缓存10分钟）
        sys.io.appendMeta(o, Lang.map("expi", System.currentTimeMillis() + 600 * 1000));
    }

    private void do_POST(WnSystem sys, WnObj o, boolean debug) {
        // 分析一下微信的消息
        WxInMsg im = Wxs.convert(sys.io.getInputStream(o, 0));

        // 检查一下配置的主目录
        String pnb = im.getToUserName();
        WnObj wxHome = sys.io.check(null, Wn.normalizeFullPath("~/.weixin/" + pnb, sys));

        // 首先查查有没有可用的上下文
        String openid = im.getFromUserName();
        String ctxPath = "context/" + openid;
        WnObj ctxHome = sys.io.fetch(wxHome, ctxPath);

        // 如果上下文里有后续命令，就执行这个命令模板
        final List<String> cmdTmpls = new ArrayList<String>(5);
        if (null != ctxHome) {
            String next_cmd = ctxHome.getString("next_cmd");
            if (!Strings.isBlank(next_cmd))
                cmdTmpls.add(next_cmd);
        }

        // 否则从 wxconf 里寻找对应的命令模板
        if (cmdTmpls.isEmpty()) {
            WnObj oConf = sys.io.check(wxHome, "wxconf");
            WxConf conf = sys.io.readJson(oConf, WxConf.class);

            // 迭代所有的 handlers
            if (null != conf.handlers)
                for (WxMsgHandler hdl : conf.handlers) {
                    if (hdl.isMatched(im)) {
                        Lang.each(hdl.command, new Each<String>() {
                            public void invoke(int index, String ele, int length) {
                                if (!Strings.isBlank(ele))
                                    cmdTmpls.add(ele);
                            }
                        });
                        // 看看是否需要生成上下文
                        if (hdl.context && null == ctxHome) {
                            ctxHome = sys.io.create(wxHome, ctxPath, WnRace.DIR);
                        }
                        break;
                    }
                }
        }

        // 实在没有命令模板了，直接返回一条消息了事。
        if (cmdTmpls.isEmpty()) {
            if (debug) {
                WxOutMsg om = Wxs.respText(im.getFromUserName(), "Get! " + Json.toJson(im));
                Wxs.fix(im, om);
                String xml = Wxs.asXml(om);
                sys.out.println(xml);
            }
            return;
        }

        // 将命令上下文对象的 ID 加入模板解析上下文
        if (null != ctxHome) {
            o.setOrRemove("weixin_context", ctxHome.id());
        }

        // 将消息的内容加入模板解析上下文并保存，以便后续命令读取
        WxUtil.saveToObj(im, o);
        sys.io.appendMeta(o, "^weixin_.+$");

        // 将命令模板解析上下文
        Context c = Lang.context(o);

        // 执行多条命令
        for (String cmdTmpl : cmdTmpls) {
            // 将模板展开成真正的命令
            String cmd = Segments.replace(cmdTmpl, c);

            // 执行命令，子命令会向标准输出里写入内容
            sys.exec(cmd);
        }

    }

    private void do_GET(WnSystem sys, WnObj o) {
        // TODO 这里的token 需要从公众号的配置中获取
        // String token = "";
        // String signature = o.getString("http-qs-signature");
        // String timestamp = o.getString("http-qs-timestamp");
        // String nonce = o.getString("http-qs-nonce");
        // Wxs.check(token, signature, timestamp, nonce);
        sys.out.println(o.getString("http-qs-echostr"));
    }
}
