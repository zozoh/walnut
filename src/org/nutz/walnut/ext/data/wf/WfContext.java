package org.nutz.walnut.ext.data.wf;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.wf.bean.WnWorkflow;
import org.nutz.walnut.ext.data.wf.util.Wfs;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.Wn;

public class WfContext extends JvmFilterContext {

    public WnWorkflow workflow;

    public NutMap vars;

    public WfContext() {
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

    public void loadWorkflow(String ph) {
        WnObj o = Wn.checkObj(sys, ph);
        String json = sys.io.readText(o);
        this.workflow = Json.fromJson(WnWorkflow.class, json);
    }

}
