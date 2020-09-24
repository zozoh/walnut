package org.nutz.walnut.ext.hookx;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class HookXContext extends JvmFilterContext {

    public List<WnObj> objs;

    public HookXContext() {
        objs = new LinkedList<WnObj>();
    }

}
