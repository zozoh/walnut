package com.site0.walnut.ext.data.sqlx.hdl;

import java.lang.reflect.Array;
import java.util.Collection;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.processor.SqlExecResult;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class sqlx_json extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String as = params.getString("as", "raw");
        Object re = fc.result;

        // 自动处理
        if (null != re) {
            if ("auto".equals(as)) {
                if (re instanceof SqlExecResult) {
                    re = ((SqlExecResult) re).toBriefBean();
                }
                // 数组
                else if (re.getClass().isArray()) {
                    int N = Array.getLength(re);
                    if (N == 0) {
                        re = null;
                    } else if (N == 1) {
                        re = Array.get(re, 0);
                    }

                }
                // 集合
                else if (re instanceof Collection<?>) {
                    Collection<?> col = (Collection<?>) re;
                    int N = col.size();
                    if (N == 0) {
                        re = null;
                    } else if (N == 1) {
                        re = col.iterator().next();
                    }
                }
            }
            // 强制获取第一个元素
            else if ("obj".equals(as)) {
                // 数组
                if (re.getClass().isArray()) {
                    int N = Array.getLength(re);
                    if (N == 0) {
                        re = null;
                    } else if (N > 0) {
                        re = Array.get(re, 0);
                    }
                }
                // 集合
                else if (re instanceof Collection<?>) {
                    Collection<?> col = (Collection<?>) re;
                    int N = col.size();
                    if (N == 0) {
                        re = null;
                    } else if (N > 0) {
                        re = col.iterator().next();
                    }
                }
            }
        }

        // 输出结果
        JsonFormat jfmt = Cmds.gen_json_format(params);
        String str = Json.toJson(re, jfmt);
        sys.out.println(str);

        fc.quiet = true;
    }

}
