package com.site0.walnut.ext.sys.httpapi.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.Nums;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WalkMode;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.httpapi.HttpApiContext;
import com.site0.walnut.ext.sys.httpapi.HttpApis;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.TextTable;
import com.site0.walnut.impl.box.WnSystem;

public class httpapi_list implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        HttpApis.doApi(sys, hc, new Callback<HttpApiContext>() {
            public void invoke(HttpApiContext c) {
                // 准备输出表
                final TextTable tt = new TextTable(5);
                tt.setShowBorder(true);
                // tt.setCellSpacing(2);

                tt.addRow(Lang.array("M", "ContentType", "Path", "Return", "Params"));
                tt.addHr();

                // 依次循环 api 并输出
                final int[] re = Nums.array(0);
                sys.io.walk(c.oApiDir, new Callback<WnObj>() {
                    public void invoke(WnObj oApi) {
                        // M:方法
                        String method = oApi.getString("api_method", "GET");

                        // Path:路径
                        String rph = Disks.getRelativePath(c.oApiDir.path(), oApi.path());

                        // Re:返回值类型
                        String reType = oApi.getString("api_return", "?");

                        // Params:参数
                        NutMap params = oApi.getAs("params", NutMap.class);
                        String paKeys = "-none-";
                        if (null != params && params.size() > 0) {
                            paKeys = Strings.join(", ", params.keySet());
                        }

                        // CT: 内容类型
                        String contentType = oApi.getBoolean("http-dynamic-header") ? "Dynamic"
                                                                                    : oApi.getString("http-header-Content-Type",
                                                                                                     "text/html");

                        // 输出
                        tt.addRow(Lang.array(method, contentType, rph, reType, paKeys));

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
