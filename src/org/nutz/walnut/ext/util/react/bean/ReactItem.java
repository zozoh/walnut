package org.nutz.walnut.ext.util.react.bean;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class ReactItem {

    private String name;

    private WnMatch _wm;

    private Object test;

    private boolean override;

    /**
     * 为上下文增加更多的变量。特别适合根据引用读取对象全部元数据等场景
     * <ul>
     * <li><code>String</code>
     * </ul>
     */
    private NutMap vars;

    private ReactAction[] actions;

    public boolean isMatch(Object input) {
        if (null == _wm) {
            return true;
        }
        return _wm.match(input);
    }

    public String getDisplayName() {
        if (this.hasName()) {
            return this.name;
        }
        return Ws.join(actions, ";");
    }

    public boolean isSameName(ReactItem it) {
        if (this.hasName() && it.hasName()) {
            return this.name.equals(it.name);
        }
        return false;
    }

    public boolean hasName() {
        return !Ws.isBlank(this.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getTest() {
        return test;
    }

    public void setTest(Object test) {
        this.test = test;
        this._wm = new AutoMatch(test);
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public boolean hasVars() {
        return null != this.vars && !this.vars.isEmpty();
    }

    public NutMap getVars() {
        return vars;
    }

    public void setVars(NutMap vars) {
        this.vars = vars;
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
