package com.site0.walnut.ext.data.titanium.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoStrMatch;

@JvmHdlParamArgs(value = "cqn", regex = "^(html)$")
public class ti_webdeps implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String url = hc.params.getString("url", "/gu/rs/ti/deps/");
        String prefix = hc.params.getString("prefix", "@deps:");
        String[] pathList = hc.params.vals;

        String ignore = hc.params.getString("ignore", null);

        List<NutMap> list = getWebDepsList(sys.io, url, prefix, ignore, pathList);

        String output;
        if (hc.params.is("html")) {
            output = renderHtml(list);
        } else {
            output = Json.toJson(list, hc.jfmt);
        }

        sys.out.println(output);
    }

    public static String renderHtml(List<? extends NutBean> depsList) {
        StringBuilder depsHtml = new StringBuilder();
        for (NutBean deps : depsList) {
            String type = deps.getString("type");
            String path = deps.getString("path");
            if ("css".equals(type)) {
                depsHtml.append("<link rel=\"stylesheet\" href=\"" + path + "\"/>\n");
            } else if ("js".equals(type)) {
                depsHtml.append("<script src=\"" + path + "\"></script>\n");
            }
        }
        return depsHtml.toString();
    }

    public static List<NutMap> getWebDepsList(WnIo io,
                                              String url,
                                              String prefix,
                                              String ignore,
                                              String... pathList) {
        WnMatch ignoreBy = null;
        if (!Ws.isBlank(ignore)) {
            ignoreBy = new AutoStrMatch(Ws.trim(ignore));
        }
        List<NutMap> list = new LinkedList<>();
        for (String str : pathList) {
            String[] phs = Ws.splitIgnoreBlank(str, "[;,:]+");
            for (String ph : phs) {
                WnObj oDeps = io.fetch(null, ph);
                if (null != oDeps) {
                    NutMap[] depsAry = io.readJson(oDeps, NutMap[].class);
                    if (null != depsAry) {
                        for (NutMap deps : depsAry) {
                            String path = deps.getString("path");
                            if (null != ignoreBy) {
                                if (ignoreBy.match(path)) {
                                    continue;
                                }
                            }
                            if (Ws.isBlank(path))
                                continue;

                            if (path.startsWith(prefix)) {
                                path = path.substring(prefix.length());
                                deps.put("path", url + path);
                            }

                            list.add(deps);
                        }
                    }
                }
            }
        }
        return list;
    }

}
