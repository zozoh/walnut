package org.nutz.walnut.ext.data.sqlx.hdl;

import org.nutz.walnut.ext.data.sqlx.SqlxContext;
import org.nutz.walnut.ext.data.sqlx.SqlxFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class sqlx_cache extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^reset$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        if (params.is("reset")) {
            fc.sqls.reset();
        }
        // 查看模式
        else {
            fc.quiet = true;
            String str = fc.sqls.toString();
            sys.out.println(str);
        }
    }

}
