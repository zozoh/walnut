package org.nutz.walnut.ext.data.wf;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.wf.bean.WfNodeType;
import org.nutz.walnut.ext.data.wf.bean.WnWorkflow;
import org.nutz.walnut.ext.data.wf.util.Wfs;
import org.nutz.walnut.ext.data.wf.vars.WfStaticVarLoader;
import org.nutz.walnut.ext.data.wf.vars.WfVarLoader;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;

public class WfContext extends JvmFilterContext {

    public NutMap input;

    public WnWorkflow workflow;

    private List<WfVarLoader> varLoaders;

    public NutMap vars;

    public WfContext() {
        this.input = new NutMap();
        this.vars = new NutMap();
        this.varLoaders = new LinkedList<>();
    }

    public void addVarLoader(WfVarLoader loader) {
        this.varLoaders.add(loader);
    }

    @SuppressWarnings("unchecked")
    public void addStaticVarLoader(String varName, String json, WnMatch picker) {
        Object v = Json.fromJson(json);

        // 防守
        if (null == v) {
            return;
        }

        NutMap map;
        // 如果是集合
        if (v instanceof Collection<?>) {
            // 收缩在一个集合里
            int n = ((Collection<?>) v).size();
            map = Wlang.map("list", v).setv("count", n);
            WfStaticVarLoader loader = new WfStaticVarLoader(varName, map);
            loader.setKeyPicker(picker);
            return;
        }
        //
        // 其他的当作对象来处理
        //
        map = NutMap.WRAP((Map<String, Object>) v);
        WfStaticVarLoader loader = new WfStaticVarLoader(varName, map);
        loader.setKeyPicker(picker);
        this.varLoaders.add(loader);

    }

    public NutBean reloadVars() {
        for (WfVarLoader loader : varLoaders) {
            loader.loadTo(vars);
        }
        return vars;
    }

    public boolean hasWorkflow() {
        return null != workflow;
    }

    public boolean hasCurrentName() {
        return vars.has(Wfs.K_CURRENT_NAME);
    }

    public String getCurrentName() {
        return vars.getString(Wfs.K_CURRENT_NAME);
    }

    public void setCurrentName(String name) {
        vars.put(Wfs.K_CURRENT_NAME, name);
    }

    public boolean hasNextName() {
        return vars.has(Wfs.K_NEXT_NAME);
    }

    public String getNextName() {
        return vars.getString(Wfs.K_NEXT_NAME);
    }

    public void setNextName(String name) {
        vars.put(Wfs.K_NEXT_NAME, name);
    }

    public void setNextType(WfNodeType type) {
        vars.put(Wfs.K_NEXT_TYPE, type);
    }

    public boolean tryLoadWorkflowFromObj(String ph, String getBy) {
        WnObj o = Wn.getObj(sys, ph);
        if (null == o) {
            return false;
        }
        String json = sys.io.readText(o);
        NutMap map = Json.fromJson(NutMap.class, json);
        loadWorkflow(map, getBy);
        return true;
    }

    @SuppressWarnings("unchecked")
    public void loadWorkflow(Map<String, Object> map, String getBy) {
        // 采用部分字段转换
        if (!Ws.isBlank(getBy)) {
            Object cell = Mapl.cell(map, getBy);
            Map<String, Object> c2 = (Map<String, Object>) cell;
            NutMap m2 = NutMap.WRAP(c2);
            this.workflow = new WnWorkflow(m2);
        }
        // 顶级转换
        else {
            NutMap m2 = NutMap.WRAP(map);
            this.workflow = new WnWorkflow(m2);
        }
    }

}
