package com.site0.walnut.ext.data.o.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.bean.WnBeanMapping;
import com.site0.walnut.util.explain.WnExplain;
import com.site0.walnut.util.explain.WnExplains;

/**
 * 将对象进行转换，顺序是
 * 
 * <ol>
 * <li><code>mapping<code>
 * <li><code>explain<code>
 * </ol>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnObjTrans {

    private WnBeanMapping mapping;

    private boolean mappingOnly;

    private WnExplain explain;

    public void loadFrom(WnSystem sys, ZParams params) {
        //
        // Prepare Mapping
        //
        loadMappingFrom(sys, params);

        //
        // Prepare WnExplain
        //
        loadExplainFrom(sys, params);

    }

    public void loadExplainFrom(WnSystem sys, ZParams params) {
        String exs = params.getString("explain", null);
        if (params.has("explainBy")) {
            String exPh = params.getString("explainBy");
            WnObj oEx = Wn.checkObj(sys, exPh);
            exs = sys.io.readText(oEx);
        }
        Object exo = exs;
        if (!Ws.isBlank(exs) && (Ws.isQuoteBy(exs, '[', ']') || Ws.isQuoteBy(exs, '{', '}'))) {
            exo = Json.fromJson(exs);
        }
        if (null != exo) {
            explain = WnExplains.parse(exo);
        }
    }

    public void loadMappingFrom(WnSystem sys, ZParams params) {
        mappingOnly = params.is("only");
        String mph = params.getString("mapping", null);
        if (!Ws.isBlank(mph)) {
            WnObj oM = Wn.checkObj(sys, mph);
            String json = sys.io.readText(oM);
            NutMap map = Json.fromJson(NutMap.class, json);
            String by = params.getString("by");
            if (!Ws.isBlank(by)) {
                map = map.getAs(by, NutMap.class);
            }
            WnBeanMapping bm = new WnBeanMapping();
            Map<String, NutMap[]> caches = new HashMap<>();
            NutBean vars = sys.session.getEnv();
            bm.setFields(map, sys.io, vars, caches);

            this.mapping = bm;
        }
    }

    public List<Object> translate(List<WnObj> objs) {
        List<Object> list = new ArrayList<>(objs.size());
        if (null == objs || objs.isEmpty()) {
            return list;
        }

        for (WnObj obj : objs) {
            NutBean bean;
            //
            // Try Mapping
            //
            if (null != mapping) {
                bean = mapping.translate(obj, mappingOnly);
            } else {
                bean = obj;
            }

            //
            // Try Explain
            //
            Object re = bean;
            if (null != explain) {
                re = explain.explain(bean);
            }
            list.add(re);
        }
        return list;
    }

    public boolean hasMapping() {
        return null != mapping;
    }

    public WnBeanMapping getMapping() {
        return mapping;
    }

    public void setMapping(WnBeanMapping mapping) {
        this.mapping = mapping;
    }

    public boolean isMappingOnly() {
        return mappingOnly;
    }

    public void setMappingOnly(boolean mappingOnly) {
        this.mappingOnly = mappingOnly;
    }

    public boolean hasExplain() {
        return null != explain;
    }

    public WnExplain getExplain() {
        return explain;
    }

    public void setExplain(WnExplain explain) {
        this.explain = explain;
    }

}
