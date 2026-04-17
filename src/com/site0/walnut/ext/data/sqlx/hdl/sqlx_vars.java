package com.site0.walnut.ext.data.sqlx.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.SqlxVarsMode;
import com.site0.walnut.ext.data.sqlx.util.SqlVarsFaker;
import com.site0.walnut.ext.data.sqlx.util.SqlVarsPutting;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class sqlx_vars extends SqlxFilter {

    private static final Log log = Wlog.getCMD();

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(explain|reset|merge|view)$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String as_mode = params.getString("as", "auto");
        String omit = params.getString("omit");
        String pick = params.getString("pick");
        String[] omits = Ws.splitIgnoreBlank(omit);
        String[] picks = Ws.splitIgnoreBlank(pick);
        SqlVarsPutting[] puttings = SqlVarsPutting
            .parse(params.getString("put"));
        boolean reset = params.is("reset");
        boolean merge = params.is("merge");
        if (reset) {
            fc.resetVarMap();
            fc.resetVarList();
        }

        // 自动决定模式
        String mode;
        if ("auto".equals(as_mode)) {
            Object input = fc.getInput();
            if (null == input) {
                mode = "map";
            } else {
                Mirror<?> mi = Mirror.me(input);
                if (mi.isColl()) {
                    mode = "list";
                } else {
                    mode = "map";
                }
            }
        }
        // 调用者直接指定了模式
        else {
            mode = as_mode;
        }

        if (log.isDebugEnabled()) {
            log.debugf("sqlx.vars[as_mode=%s;mode=%s]: %s",
                       as_mode,
                       mode,
                       Ws.join(params.vals, " "));
        }

        // For List
        if ("list".equals(mode)) {
            List<? extends NutBean> list = __read_as_list(sys, fc, params);
            fc.setVarMode(SqlxVarsMode.LIST);

            // 额外读取值
            NutBean pipeContext = fc.getPipeContext();
            for (NutBean bean : list) {
                SqlVarsPutting.apply(puttings, bean, pipeContext);
            }

            // 计入上下文
            fc.appendVarList(list, picks, omits);
        }
        // For Map
        else {
            NutMap map = __read_as_map(sys, fc, params);
            fc.setVarMode(SqlxVarsMode.MAP);

            // 额外读取值
            NutBean pipeContext = fc.getPipeContext();
            SqlVarsPutting.apply(puttings, map, pipeContext);

            if (merge) {
                fc.mergeVarMap(map, picks, omits);
            } else {
                fc.assignVarMap(map, picks, omits);
            }
        }

        if (params.is("explain")) {
            fc.explainVars();
        }

        if (params.is("view")) {
            fc.result = Wlang.map("map", fc.getVarMap())
                .setv("list", fc.getVarList());
        }
    }

    private List<? extends NutBean> __read_as_list(WnSystem sys,
                                                   SqlxContext fc,
                                                   ZParams params) {
        // 伪造列表数据
        if (params.has("fake")) {
            SqlVarsFaker faker = new SqlVarsFaker(params.getString("fake"));
            faker.setLang(params.getString("lang", "zh_cn"));
            return faker.genList(sys);
        }
        // 直接采用上下文
        if (params.vals.length == 0) {
            return fc.getInputAsList();
        }
        // 逐个解析参数
        ArrayList<NutMap> beans = new ArrayList<>(params.vals.length);
        for (String val : params.vals) {
            if (val.startsWith("=") || val.startsWith(":")) {
                String key = val.substring(1).trim();
                if ("..".equals(key)) {
                    beans.addAll(fc.getInputAsList());
                } else {
                    List<NutMap> list = fc.getInputOrPipeVarAsList(key);
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
            return fc.getInputAsMap();
        }
        // 逐个解析参数
        NutMap re = new NutMap();
        for (String val : params.vals) {
            if (val.startsWith("=") || val.startsWith(":")) {
                String key = val.substring(1).trim();
                if ("..".equals(key)) {
                    NutMap inputMap = fc.getInputAsMap();
                    if (null != inputMap) {
                        re.putAll(fc.getInputAsMap());
                    }
                } else {
                    NutMap vmap = fc.getInputOrPipeVarAsMap(key);
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
