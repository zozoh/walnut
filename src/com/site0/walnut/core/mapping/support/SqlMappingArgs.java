package com.site0.walnut.core.mapping.support;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public class SqlMappingArgs {
    public String daoName;
    public String entityName;
    public NutMap options;

    public SqlMappingArgs(String str) {
        String[] ss = Ws.splitTrimedEmptyAsNull(str, ":", 3);
        this.daoName = Ws.sBlank(ss[0], "default");
        this.entityName = ss[1];

        String flt = ss.length >= 3 ? ss[2] : null;
        if (!Ws.isBlank(flt)) {
            this.options = Wlang.map(flt);
        }
    }
}
