package com.site0.walnut.ext.sys.redis;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;

import com.site0.walnut.ext.sys.redis.hdl.RedisHanlder;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;

public class cmd_redis extends JvmFilterExecutor<RedisContext, RedisFilter> {

    public cmd_redis() {
        super(RedisContext.class, RedisFilter.class);
    }

    @Override
    protected RedisContext newContext() {
        return new RedisContext();
    }

    @Override
    protected void prepare(WnSystem sys, RedisContext fc) {
        // 获取配置信息(连接信息)
        fc.config = Wedis.loadConfigByName(sys, fc.params.get("conf"));

        // 分析结果集键的渲染方式
        String rk = fc.params.val(0);
        if (null != rk) {
            int pos = 0;
            for (; pos < rk.length(); pos++) {
                char c = rk.charAt(pos);
                if ('I' != c && 'K' != c && 'A' != c) {
                    break;
                }
            }
            if (pos > 0) {
                fc.setResultKey(rk.substring(0, pos));
            }
            String sep = rk.substring(pos);
            if (null != sep && sep.length() > 0) {
                fc.setResultKeySep(rk.substring(pos));
            }
        }
    }

    @Override
    protected void output(WnSystem sys, RedisContext fc) {
        List<RedisHanlder> hdls = fc.getHandlers();
        if (hdls.size() > 0) {
            Wedis.run(fc.config, jed -> {
                for (RedisHanlder h : hdls) {
                    Object val = h.run(fc, jed);
                    fc.setResult(h, val);
                }
            });
        }
        JsonFormat jfmt = Cmds.gen_json_format(fc.params);
        sys.out.println(Json.toJson(fc.result, jfmt));
    }

}
