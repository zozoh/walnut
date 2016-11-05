package org.nutz.walnut.ext.wup.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 添加一个更新包
 * @author wendal
 *
 */
public class wup_pkg_add implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String name = hc.params.get("name");
        String version = hc.params.get("version");
        String source = hc.params.vals[0];
        source = Wn.normalizeFullPath(source, sys);
        if (!source.endsWith(".tgz")) {
            sys.err.println("must be tgz file");
            return;
        }
        WnObj src = sys.io.check(null, source);
        if (Strings.isBlank(name) || Strings.isBlank(version)) {
            String[] tmp = src.name().split("-", 2);
            if (Strings.isBlank(name))
                name = tmp[0];
            if (Strings.isBlank(version)) {
                version = tmp[1].substring(0, tmp[1].length() - 4);
            }
        }
        
        sys.out.printf("name=%s version=%s source=%s\r\n", name, version, source);
        String path = Wn.normalizePath("~/wup/pkgs/"+name+"/"+version+".tgz", sys);
        WnObj obj = sys.io.createIfNoExists(null, path, WnRace.FILE);
        sys.io.copyData(src, obj);
    }

}
