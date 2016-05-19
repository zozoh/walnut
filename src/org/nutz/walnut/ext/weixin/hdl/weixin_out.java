package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.weixin.bean.WxArticle;
import org.nutz.weixin.bean.WxInMsg;
import org.nutz.weixin.bean.WxOutMsg;
import org.nutz.weixin.util.Wxs;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 输出一段简单文本
 * weixin xxx out "text:Hello world" -openid xxxx
 * 
 * # 输出一段链接
 * weixin xxx out "article:Hello;;brief;;http://xxxxx" -openid xxx
 * 
 * # 输出更复杂的消息
 * weixin xxx out "{..}" -openid xxx
 * 
 * # 根据直接回复消息
 * weixin out "text:xxxxx" -inmsg id:xxxx
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class weixin_out implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 得到输出的内容
        String out = hc.params.vals.length > 0 ? hc.params.vals[0] : null;

        // 如果没有输出内容
        if (Strings.isBlank(out)) {
            // 从管线里读取纯文本内容
            if (sys.pipeId > 0) {
                out = Strings.trim(sys.in.readAll());
            }
            // 否则抛错
            else {
                throw Er.create("e.cmd.weixin.out.noinput");
            }
        }

        WxOutMsg om = null;

        String openid = hc.params.get("openid");
        String pnb = hc.oHome == null ? null : hc.oHome.name();

        // 一个 JSON
        if (Strings.isQuoteBy(out, '{', '}')) {
            om = Json.fromJson(WxOutMsg.class, out);
        }
        // 一个简单的文本
        else if (out.startsWith("text:")) {
            om = Wxs.respText(openid, out.substring("text:".length()));
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
        // 就是一段简单的文本
        else {
            om = Wxs.respText(openid, out);
        }

        // 设置创建时间
        om.setCreateTime(System.currentTimeMillis() / 1000);

        if (!Strings.isBlank(openid))
            om.setToUserName(openid);

        if (!Strings.isBlank(pnb))
            om.setFromUserName(pnb);

        // 如果指明了输入源，则视图覆盖 from/toUserName
        String inmsg = hc.params.get("inmsg");
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

}
