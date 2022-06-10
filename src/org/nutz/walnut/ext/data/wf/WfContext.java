package org.nutz.walnut.ext.data.wf;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.wf.bean.WnWorkflow;
import org.nutz.walnut.ext.data.wf.util.Wfs;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class WfContext extends JvmFilterContext {

    public NutMap input;

    public WnWorkflow workflow;

    public NutMap vars;

    public WfContext() {
        this.input = new NutMap();
        this.vars = new NutMap();
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
