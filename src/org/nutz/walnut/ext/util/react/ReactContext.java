package org.nutz.walnut.ext.util.react;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.util.react.bean.ReactConfig;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class ReactContext extends JvmFilterContext {

    public NutBean vars;

    public ReactConfig config;

    public NutMap result;

    public ReactContext() {
        this.config = new ReactConfig();
        this.vars = new NutMap();
        this.result = new NutMap();
    }

}
