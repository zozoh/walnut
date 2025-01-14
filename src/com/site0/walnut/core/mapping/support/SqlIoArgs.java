package com.site0.walnut.core.mapping.support;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

public class SqlIoArgs {
    public String daoName;
    public String entityName;
    public SqlIoOptions options;
    public NutMap filter;

    public SqlIoArgs(String str) {
        String[] ss = Ws.splitTrimedEmptyAsNull(str, ":", 4);
        this.daoName = Ws.sBlank(ss[0], "default");
        this.entityName = ss[1];

        // 选项
        String _options = ss.length >= 3 ? ss[2] : null;
        this.options = new SqlIoOptions(_options);

        // 过滤条件
        String _flt = ss.length >= 4 ? ss[3] : null;
        if (!Ws.isBlank(_flt)) {
            this.filter = Wlang.map(_flt);
        }
    }
}
