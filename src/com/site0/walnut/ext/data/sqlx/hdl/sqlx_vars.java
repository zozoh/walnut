package com.site0.walnut.ext.data.sqlx.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.util.SqlVarsFaker;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class sqlx_vars extends SqlxFilter {

    private static final Log log = Wlog.getCMD();

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(explain|reset|merge)$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String mode = params.getString("as", "map");
        String omit = params.getString("omit");
        String pick = params.getString("pick");
        String[] omits = Ws.splitIgnoreBlank(omit);
        String[] picks = Ws.splitIgnoreBlank(pick);
        boolean reset = params.is("reset");
        boolean merge = params.is("merge");
        if (reset) {
            fc.resetVarMap();
            fc.resetVarList();
        }

        if (log.isDebugEnabled()) {
            log.debugf("sqlx.vars: %s", Ws.join(params.vals, " "));
        }

        if ("list".equals(mode)) {
            List<NutBean> list = __read_as_list(sys, fc, params);
            fc.appendVarList(list, picks, omits);
        } else {
            NutMap map = __read_as_map(sys, fc, params);
            if (merge) {
                fc.mergeVarMap(map, picks, omits);
            } else {
                fc.assignVarMap(map, picks, omits);
            }
        }

        if (params.is("explain")) {
            fc.explainVars();
        }
    }

    private List<NutBean> __read_as_list(WnSystem sys, SqlxContext fc, ZParams params) {
        // 伪造列表数据
        if (params.has("fake")) {
            SqlVarsFaker faker = new SqlVarsFaker(params.getString("fake"));
            faker.setLang(params.getString("lang", "zh_cn"));
            return faker.genList(sys);
        }
        // 直接采用上下文
        if (params.vals.length == 0) {
            return Wlang.list(fc.getInput());
        }
        // 逐个解析参数
        ArrayList<NutBean> beans = new ArrayList<>(params.vals.length);
        for (String val : params.vals) {
            if (val.startsWith("=") || val.startsWith(":")) {
                String key = val.substring(1).trim();
                if ("..".equals(key)) {
                    beans.add(fc.getInput());
                } else {
                    List<NutMap> list = fc.getInputVarAsList(key);
                    for (NutMap li : list) {
                        beans.add(li);
                    }
                }
            } else {
                NutMap map = Wlang.map(val);
                beans.add(map);
            }
        }
        return beans;
    }

    private NutMap __read_as_map(WnSystem sys, SqlxContext fc, ZParams params) {
        // 伪造列表数据
        if (params.has("fake")) {
            SqlVarsFaker faker = new SqlVarsFaker(params.getString("fake"));
            faker.setLang(params.getString("lang", "zh_cn"));
            return faker.genBean(sys);
        }
        // 从标准输入读取
        if (params.vals.length == 0) {
            return fc.getInput();
        }
        // 逐个解析参数
        NutMap re = new NutMap();
        for (String val : params.vals) {
            if (val.startsWith("=") || val.startsWith(":")) {
                String key = val.substring(1).trim();
                if ("..".equals(key)) {
                    re.putAll(fc.getInput());
                } else {
                    NutMap vmap = fc.getInputVarAsMap(key);
                    re.putAll(vmap);
                }
            } else {
                NutMap map = Wlang.map(val);
                re.putAll(map);
            }
        }
        return re;
    }

}
