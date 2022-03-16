package org.nutz.walnut.ext.data.wf.util;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class WfEdge {

    private Object test;

    private ReactAction[] actions;

    private WnMatch _wm;

    public WnMatch getMatch() {
        if (null == _wm) {
            _wm = AutoMatch.parse(test, true);
        }
        return _wm;
    }

    public boolean isOn(NutMap vars) {
        WnMatch wm = this.getMatch();
        return wm.match(vars);
    }

    public Object getTest() {
        return test;
    }

    public void setTest(Object test) {
        this.test = test;
    }

    public boolean hasActions() {
        return null != actions && actions.length > 0;
    }

    public ReactAction[] getActions() {
        return actions;
    }

    public void setActions(ReactAction[] actions) {
        this.actions = actions;
    }

}
