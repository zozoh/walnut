package com.site0.walnut.ext.data.sqlx.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.sqlx.SqlxContext;
import com.site0.walnut.ext.data.sqlx.SqlxFilter;
import com.site0.walnut.ext.data.sqlx.util.SqlVarsFaker;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class sqlx_vars extends SqlxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(update|reset)$");
    }

    @Override
    protected void process(WnSystem sys, SqlxContext fc, ZParams params) {
        String mode = params.getString("as", "map");
        String omit = params.getString("omit");
        String pick = params.getString("pick");
        String[] omits = Ws.splitIgnoreBlank(omit);
        String[] picks = Ws.splitIgnoreBlank(pick);
        boolean reset = params.is("reset");
        if (reset) {
            fc.resetVarMap();
            fc.resetVarList();
        }

        if ("list".equals(mode)) {
            List<NutBean> list = __read_as_list(sys, fc, params);
            fc.appendVarList(list, picks, omits);
        } else {
            NutMap map = __read_as_map(sys, fc, params);
            fc.appendVarMap(map, picks, omits);
        }

        if (params.is("update")) {
            fc.prepareForUpdate();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<NutBean> __read_as_list(WnSystem sys, SqlxContext fc, ZParams params) {
        // 伪造列表数据
        if (params.has("fake")) {
            SqlVarsFaker faker = new SqlVarsFaker(params.getString("fake"));
            faker.setLang(params.getString("lang", "zh_cn"));
            return faker.genList(sys);
        }
        // 从标准输入读取
        if (params.vals.length == 0) {
            String json = sys.in.readAll();
            List list = Json.fromJsonAsList(NutMap.class, json);
            return (List<NutBean>) list;
        }
        // 逐个解析参数
        ArrayList<NutBean> beans = new ArrayList<>(params.vals.length);
        for (String val : params.vals) {
            if ("~STDIN~".equals(val)) {
                String json = sys.in.readAll();
                List<NutMap> list = Json.fromJsonAsList(NutMap.class, json);
                beans.addAll(list);
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
            String json = sys.in.readAll();
            NutMap map = Json.fromJson(NutMap.class, json);
            return map;
        }
        // 逐个解析参数
        NutMap re = new NutMap();
        for (String val : params.vals) {
            if ("~STDIN~".equals(val)) {
                String json = sys.in.readAll();
                NutMap map = Json.fromJson(NutMap.class, json);
                re.putAll(map);
            } else {
                NutMap map = Wlang.map(val);
                re.putAll(map);
            }
        }
        return re;
    }

}
