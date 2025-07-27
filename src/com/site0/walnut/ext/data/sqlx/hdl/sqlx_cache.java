package com.site0.walnut.ext.data.sqlx.hdl;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class sqlx_cache extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^reset$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        if (params.is("reset")) {
            fc.quiet = true;
            fc.sqls.clear();
            sys.out.printlnf("clear done: %s", fc.sqls.size());
        }
        // 查看模式
        else {
            fc.quiet = true;
            String str = fc.sqls.toString();
            sys.out.println(str);
        }
    }

}
