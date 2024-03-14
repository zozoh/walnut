package com.site0.walnut.ext.data.esi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.nutz.ioc.Ioc;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnHttpResponseWriter;

public class cmd_esi extends JvmHdlExecutor {

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        // 记录从哪里 copy args 的位置
        int pos;

        // 没有参数
        if (hc.args.length == 0) {
            throw Er.create("e.cmd.esi.lackArgs", hc.args);
        }
        // 第一个参数就是 hdl，那么当前目录就作为 oHome
        // :> thing hdlName xxx
        else if (null != this.getHdl(hc.args[0])) {
            hc.oRefer = sys.getCurrentObj();
            hc.hdlName = hc.args[0];
            pos = 1;
        }
        // 第一个参数表示一个 TsID 并且有多余一个的参数
        // :> thing ID hdlName xxx
        else if (hc.args.length >= 2) {
            hc.oRefer = sys.io.check(null, Wn.normalizeFullPath(hc.args[0], sys));
            hc.hdlName = hc.args[1];
            pos = 2;
        }
        // 否则还是缺参数
        else {
            throw Er.create("e.cmd.esi.lackArgs", hc.args);
        }

        // Copy 剩余参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);

        hc.setv("conf", conf(sys, hc));
    }

    // 跟thing命令一模一样
    @SuppressWarnings("unchecked")
    @Override
    protected void _before_quit(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 输出
        if (!hc.params.is("Q")) {
            // 输出内容
            if (null != hc.output) {
                // 如果是 WnObj ..
                if (hc.output instanceof WnObj) {
                    Cmds.output_objs(sys, hc.params, hc.pager, Lang.list((WnObj) hc.output), false);
                }
                // 如果是 NutBean
                else if (hc.output instanceof NutBean) {
                    Cmds.output_beans(sys, hc.params, hc.pager, Lang.list((NutBean) hc.output));
                }
                // 如果就是普通 Map
                else if (hc.output instanceof Map) {
                    NutMap map = NutMap.WRAP((Map<String, Object>) hc.output);
                    Cmds.output_beans(sys, hc.params, hc.pager, Lang.list(map));
                }
                // 如果是数组或者列表，直接搞
                else if (hc.output.getClass().isArray() || hc.output instanceof List) {
                    Object oFirst = Lang.first(hc.output);

                    // 确保按照列表输出
                    hc.params.setv("l", true);

                    // WnObj 的集合
                    if (null == oFirst || oFirst instanceof WnObj) {
                        Cmds.output_objs(sys,
                                         hc.params,
                                         hc.pager,
                                         (List<? extends WnObj>) hc.output,
                                         false);
                    }
                    // WnBean 的集合
                    else if (oFirst instanceof NutBean) {
                        Cmds.output_beans(sys,
                                          hc.params,
                                          hc.pager,
                                          (List<? extends NutBean>) hc.output);
                    }
                    // 其他集合只能简单的 toJson
                    else {
                        sys.out.println(Json.toJson(hc.output, hc.jfmt));
                    }
                }
                // 如果是个输入流
                else if (hc.output instanceof InputStream) {
                    InputStream ins = (InputStream) hc.output;
                    sys.out.writeAndClose(ins);
                }
                // 如果是字符串，不要强制输出换行
                else if (hc.output instanceof CharSequence) {
                    sys.out.print(hc.output.toString());
                }
                // 如果是 WnHttpResponse，那么就渲染
                else if (hc.output instanceof WnHttpResponseWriter) {
                    WnHttpResponseWriter resp = (WnHttpResponseWriter) hc.output;
                    OutputStream ops = sys.out.getOutputStream();
                    resp.writeTo(ops);
                }
                // 其他的情况，就直接 toString 输出咯
                else {
                    sys.out.println(hc.output);
                }
            }
        }
    }

    protected ElasticsearchService _service;

    public ElasticsearchService esi(Ioc ioc) {
        if (_service == null)
            _service = ioc.get(ElasticsearchService.class);
        return _service;
    }

    public EsiConf conf(WnSystem sys, JvmHdlContext hc) {
        WnObj wobj = hc.oRefer;
        EsiConf conf = esi(hc.ioc).getEsiConf(wobj);
        return conf;
    }
}
