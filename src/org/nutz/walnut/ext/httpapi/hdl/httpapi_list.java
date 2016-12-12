package org.nutz.walnut.ext.httpapi.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Nums;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.httpapi.HttpApiContext;
import org.nutz.walnut.ext.httpapi.HttpApis;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.TextTable;
import org.nutz.walnut.impl.box.WnSystem;

public class httpapi_list implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        HttpApis.doApi(sys, hc, new Callback<HttpApiContext>() {
            public void invoke(HttpApiContext c) {
                // 准备输出表
                final TextTable tt = new TextTable(5);
                tt.setShowBorder(true);
                // tt.setCellSpacing(2);

                tt.addRow(Lang.array(" ", "Path", "Ct", "Re", "Params"));
                tt.addHr();

                // 依次循环 api 并输出
                final int[] re = Nums.array(0);
                sys.io.walk(c.oApiDir, new Callback<WnObj>() {
                    public void invoke(WnObj oApi) {
                        // 路径
                        String rph = Disks.getRelativePath(c.oApiDir.path(), oApi.path());

                        // 前缀
                        String prefix = oApi.getBoolean("noapi") ? "-" : "@";

                        // 返回值类型
                        String reType = oApi.getString("retype", "~");

                        // 参数
                        Object params = oApi.get("params");
                        String json = "";
                        if (null != params)
                            json = Json.toJson(params,
                                               JsonFormat.compact()
                                                         .setQuoteName(false)
                                                         .setIgnoreNull(false));

                        // HTTP 返回类型
                        String httpRe = oApi.getBoolean("http-dynamic-header") ? "Dynamic"
                                                                               : oApi.getString("http-header-Content-Type",
                                                                                                "~");

                        // 输出
                        tt.addRow(Lang.array(prefix, rph, httpRe, reType, json));

                        // 计数
                        re[0]++;

                    }
                }, WalkMode.LEAF_ONLY);

                // 末行分隔线
                tt.addHr();

                // 输出表
                sys.out.print(tt.toString());

                // 输出计数
                sys.out.printlnf("total %d api", re[0]);
            }
        }, false, false);
    }

}
