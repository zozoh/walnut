package com.site0.walnut.ext.sys.hookx;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmFilterContext;

public class HookXContext extends JvmFilterContext {

    public List<WnObj> objs;

    public HookXContext() {
        objs = new LinkedList<WnObj>();
    }

}
