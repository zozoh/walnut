package org.nutz.walnut.ext.thing.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(drop)$")
public class thing_detail implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到对应对 Thing
        WnObj oT = Things.checkThIndex(sys, hc);

        // 清除详情
        if (hc.params.is("drop")) {
            // 情况 detail
            sys.io.writeText(oT, "");

            // 设置 thing 元数据
            oT.setv("th_detail_tp", null);
            oT.setv("th_detail_sz", 0);
            sys.io.set(oT, "^th_detail_.+$");

            // 输出被清空的对象
            hc.output = oT;
        }
        // 创建或者修改
        else if (hc.params.has("content")) {
            // 根据类型修改内容类型
            if (hc.params.has("tp")) {
                __update_th_type(sys, hc, oT);
            }
            // 直接修改内容蕾西
            else if (hc.params.has("mime")) {
                __update_th_mime(sys, hc, oT);
            }

            // 写入内容
            String content = Cmds.getParamOrPipe(sys, hc.params, "content", false);
            sys.io.writeText(oT, content);

            // 得到摘要
            String brief = hc.params.get("brief");

            // 指定了摘要
            if (!Strings.isBlank(brief)) {
                // 自动摘要 TODO 根据内容类型设计不同的摘要算法
                if ("true".equals(brief)) {
                    brief = content.substring(0, Math.min(content.length(), 256));
                }

                // 设置 thing 元数据
                oT.setv("brief", brief);
                sys.io.set(oT, "^brief$");
            }

            // 输出对象
            hc.output = oT;
        }
        // 仅仅修改摘要/内容类型
        else if (hc.params.has("brief") || hc.params.has("tp") || hc.params.has("mime")) {
            // 更新摘要
            if (hc.params.has("brief")) {
                String brief = Cmds.getParamOrPipe(sys, hc.params, "brief", false);
                // 自动摘要 TODO 根据内容类型设计不同的摘要算法
                if (Strings.isBlank(brief)) {
                    String content = Strings.sBlank(sys.io.readText(oT), "");
                    content = Strings.trim(content).replaceAll("[>+-`#\t\r\n ]", "");
                    brief = content.substring(0, Math.min(content.length(), 50));
                }
                // 如果指定为空摘要
                else if("null".equals(brief)) {
                    brief = null;
                }

                // 设置 thing 元数据
                oT.setv("brief", brief);
                sys.io.set(oT, "^brief$");
            }

            // 根据类型修改内容类型
            if (hc.params.has("tp")) {
                __update_th_type(sys, hc, oT);
            }
            // 直接修改内容类型
            else if (hc.params.has("mime")) {
                __update_th_mime(sys, hc, oT);
            }

            // 输出对象
            hc.output = oT;
        }
        // 输出 detail 对象的内容
        else {
            hc.output = sys.io.readText(oT);
        }
    }

    private void __update_th_mime(WnSystem sys, JvmHdlContext hc, WnObj oT) {
        String mime = hc.params.get("mime");
        if ("true".equals(mime))
            mime = "text/markdown";
        if (!oT.isMime(mime)) {
            sys.io.set(oT.mime(mime), "^(mime)$");
        }
    }

    private void __update_th_type(WnSystem sys, JvmHdlContext hc, WnObj oT) {
        String tp = hc.params.get("tp", "txt");
        if ("true".equals(tp))
            tp = "md";
        String mime = sys.io.mimes().getMime(tp, "text/plain");
        if (!oT.isMime(mime)) {
            sys.io.set(oT.mime(mime), "^(mime)$");
        }
    }

}
