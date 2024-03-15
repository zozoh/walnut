package com.site0.walnut.ext.net.mailx.hdl;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.util.ThQr;
import com.site0.walnut.ext.data.thing.util.ThQuery;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.ext.net.mailx.bean.WnSmtpMail;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPager;
import com.site0.walnut.util.ZParams;

public class mailx_set extends MailxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(content|read)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        // 准备 ...
        NutBean bean = null;
        NutBean meta;
        String transVars = null;

        String transKey = params.get("trans");
        if ("true".equals(transKey)) {
            transKey = "transVar";
        }

        // 读取路径
        if (params.vals.length > 0) {
            WnObj o = Wn.checkObj(sys, params.val(0));

            // 如果是目录，且需要二次查询
            if (o.isDIR()) {
                if (params.has("match")) {
                    // ThingSet
                    if (o.isType("thing_set")) {
                        WnThingService wts = new WnThingService(sys, o);
                        ThQuery tq = new ThQuery();
                        tq.qStr = params.getString("match");
                        tq.wp = new WnPager(1, 0);
                        tq.autoObj = true;
                        tq.needContent = params.is("content");
                        ThQr qr = wts.queryThing(tq);
                        bean = (NutBean) qr.data;
                    }
                    // 普通目录
                    else {
                        WnQuery q = Wn.Q.pid(o);
                        NutMap qmap = params.getMap("match");
                        q.setAll(qmap);
                        WnObj of = sys.io.getOne(q);
                        if (params.is("content") && of.isFILE()) {
                            String content = sys.io.readText(of);
                            of.put("content", content);
                        }
                        bean = of;
                    }
                }
                // 目录的话就直接用了
                else {
                    bean = o;
                }
            }
            // 如果是文件
            else if (o.isFILE()) {
                if (params.is("content")) {
                    String content = sys.io.readText(o);
                    o.put("content", content);
                    bean = o;
                }
                // Read content as json
                else if (params.is("read")) {
                    bean = sys.io.readJson(o, NutMap.class);
                }
                // Just self
                else {
                    bean = o;
                }
            }

            // 看看是否是从文件读取脚本
            if ("@content".equals(transKey)) {
                transVars = sys.io.readText(o);
            }

        }
        // 读取标准输入
        else {
            String json = sys.in.readAll();
            bean = Json.fromJson(NutMap.class, json);
        }

        // 防守
        if (null == bean)
            return;

        // 分析转换脚本
        if (!Strings.isBlank(transKey) && null == transVars) {
            transVars = bean.getString(transKey);
        }
        if (!Strings.isBlank(transVars)) {
            fc.varTrans = transVars;
        }

        // 准备映射
        String mapping = params.getString("mapping");
        if (!Strings.isBlank(mapping)) {
            NutMap translate = Wlang.map(mapping);
            meta = (NutBean) Wn.explainObj(bean, translate);
        }
        // 无需映射
        else {
            meta = bean;
        }

        // 转换到上下文中
        WnSmtpMail mail = Wlang.map2Object(meta, WnSmtpMail.class);
        fc.mail.copyFrom(mail);
    }

}
