package org.nutz.walnut.ext.data.wf.bean;

import org.nutz.json.JsonField;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.util.react.bean.ReactAction;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class WfEdge {

    private Object test;

    private ReactAction[] actions;

    @JsonField(ignore = true)
    private String fromName;

    @JsonField(ignore = true)
    private String toName;

    @JsonField(ignore = true)
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

    public void setEdgeName(String fromName, String toName) {
        this.fromName = fromName;
        this.toName = toName;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
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
