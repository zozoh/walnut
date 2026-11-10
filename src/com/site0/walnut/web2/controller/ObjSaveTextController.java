package com.site0.walnut.web2.controller;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.repo.Base64;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wn;

/**
 * <code>/o/save/text</code>：对照旧 <code>ObjModule.saveText(607)</code>（RM-NUTZ-08
 * 审核表 #9）。逐行照抄旧方法体（含 <code>whenRead</code> 处理链接文件、
 * <code>Strings.sNull</code>、base64 分支），只换绑定/视图/响应写法。过滤器
 * <code>SessionCheckFilter()</code>。
 */
public class ObjSaveTextController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        try {
            NutMap pm = params(req);
            String str = pm.getString("str");
            String content = pm.getString("content");
            boolean createIfNoExists = pm.getBoolean("cine", false);
            boolean asBase64 = pm.getBoolean("base64", false);

            // 获取当前会话
            WnSession se = Wn.WC().checkSession();
            WnIo io = io(ctx);

            // 取得对应对象
            WnObj o;
            if (createIfNoExists) {
                String aph = Wn.normalizeFullPath(str, se);
                o = io.createIfNoExists(null, aph, WnRace.FILE);
            }
            // 必须存在
            else {
                o = Wn.checkObj(io, se, str);
            }

            // 确保可读，同时处理链接文件
            o = Wn.WC().whenRead(o, false);

            // 处理空
            content = Strings.sNull(content, "");

            // 写入: base64
            if (asBase64) {
                byte[] bs = Base64.decode(content);
                io.writeBytes(o, bs);
            }
            // 直接写入文本
            else {
                io.writeText(o, content);
            }

            // 返回
            return WnMvcView.ajax(o);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

}
