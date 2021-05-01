package org.nutz.walnut.ext.data.titanium.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;

@JvmHdlParamArgs(value = "cqn", regex = "^(html)$")
public class ti_webdeps implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String url = hc.params.get("url", "/gu/rs/ti/deps/");
        String prefix = hc.params.get("prefix", "@deps:");
        String[] pathList = hc.params.vals;

        List<NutMap> list = getWebDepsList(sys.io, url, prefix, pathList);

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
                                              String... pathList) {
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
