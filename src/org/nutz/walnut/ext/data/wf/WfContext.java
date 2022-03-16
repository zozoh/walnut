package org.nutz.walnut.ext.data.wf;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.wf.util.WfData;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.Wn;

public class WfContext extends JvmFilterContext {

    public WfData workflow;

    public NutMap vars;

    public WfContext() {
        this.vars = new NutMap();
    }

    public void loadWorkflow(String ph) {
        WnObj o = Wn.checkObj(sys, ph);
        String json = sys.io.readText(o);
        this.workflow = Json.fromJson(WfData.class, json);
    }

}
