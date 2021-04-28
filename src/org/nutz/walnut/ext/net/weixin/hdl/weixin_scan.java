package org.nutz.walnut.ext.net.weixin.hdl;

import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 根据扫码信息，执行对应脚本模板，模板固定到占位符为
 * 
 * <pre>
 * {
 *      openid   : "xxxxx",   // 微信用户到 openid
 *      pnb      : "xxxxx",   // 公众号
 *      scene    : "xxxxx",   // 场景字符串或者数字
 *      eventKey : "xxxxx",   // 原始到扫码事件 KEY
 * }
 * </pre>
 * 
 * <p>
 * 命令的使用方法:
 * 
 * <pre>
 * # 执行对应脚本
 * weixin xxx scan -openid yyy -eventkey '12345678' [-dft 'default'] -scan_lock 15000
 * </pre>
 * 
 * @author wendal(wendal
 * @author zozoh(zozohtnt@gmail.com)
 */
public class weixin_scan implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // regapi的模板
        // weixin ${weixin_ToUserName} scan -openid ${weixin_FromUserName}
        // -eventkey '${weixin_EventKey}' -c

        String pnb = hc.oRefer.name();
        String openid = hc.params.check("openid");
        String eventkey = hc.params.check("eventkey");

        // 乱入?
        if (Strings.isBlank(eventkey)) {
            return;
        }

        // 第一次关注就扫描的话, 会添加前缀qrscene_ 移除之
        String scene = eventkey;
        if (eventkey.startsWith("qrscene_"))
            scene = eventkey.substring("qrscene_".length());

        // 找找有没有对应的文本,有就当命令执行一下
        WnObj obj = sys.io.fetch(hc.oRefer, "scene/" + scene);

        // 没找到，那么看看要不要执行默认到 key
        if (null == obj && hc.params.has("dft")) {
            obj = sys.io.fetch(hc.oRefer, "scene/" + hc.params.get("dft"));
        }

        // 找到了
        if (obj != null) {
            // 防止重复
            long nowInMs = Wn.now();
            if (obj.getLong("scan_lock_at") > nowInMs)
                return;
            WnObj obj2 = sys.io.setBy(Wn.Q.id(obj),
                                      "scan_lock_at",
                                      nowInMs + hc.params.getLong("scan_lock", 15000),
                                      false);
            if (obj2.getLong("scan_lock_at") > nowInMs)
                return;

            // 得到命令模版上下文
            NutMap c = new NutMap();
            c.put("openid", openid);
            c.put("pnb", pnb);
            c.put("eventkey", eventkey);
            c.put("scene", scene);
            WnAccount info = new WnAccount();
            info.setWxGhOpenId(pnb, openid);
            WnAccount usr = sys.auth.getAccount(info);
            if (usr != null) {
                c.put("uid", usr.getId());
            }

            // 读取命令模板
            String tmpl = sys.io.readText(obj);
            String cmdText = Tmpl.exec(tmpl, c);

            // 执行并输出
            sys.out.print(sys.exec2(cmdText));
        }

    }
}
