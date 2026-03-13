package com.site0.walnut.ext.data.sqlx.hdl;

import java.util.Map;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.loader.SqlEntry;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class sqlx_cache extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^clear$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        if (params.is("clear")) {
            fc.quiet = true;
            fc.sqls.clear();
            sys.out.printlnf("clear done: %s", fc.sqls.size());
            return;
        }

        String str;
        fc.quiet = true;
        // 查看模式
        String key = params.get("get");
        if (!Ws.isBlank(key)) {
            Map<String, SqlEntry> re = fc.sqls.find(key);
            str = SqlEntry.dumpToStr(re);
        }
        // 全部打印
        else {
            str = fc.sqls.toString();
        }

        // 输出
        sys.out.println(str);
    }

}
