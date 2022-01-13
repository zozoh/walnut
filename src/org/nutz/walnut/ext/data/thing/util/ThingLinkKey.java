package org.nutz.walnut.ext.data.thing.util;

import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class ThingLinkKey {

    private Object testPrimary;

    private Object testUpdate;

    private Object match;

    private boolean strict;

    private ThingLinkKeyTarget target;

    private NutMap vars;

    private NutMap set;

    private String[] run;

    public boolean isDoNothing() {
        return !this.hasSet() && !this.hasRun();
    }

    public Object getTestPrimary() {
        return testPrimary;
    }

    public void setTestPrimary(NutMap[] testPrimary) {
        this.testPrimary = testPrimary;
    }

    public boolean matchTestPrimary(NutBean meta) {
        return matchTest(meta, testPrimary);
    }

    public Object getTestUpdate() {
        return testUpdate;
    }

    public void setTestUpdate(NutMap[] testUpdate) {
        this.testUpdate = testUpdate;
    }

    public boolean matchTestUpdate(NutBean meta) {
        return matchTest(meta, testUpdate);
    }

    private boolean matchTest(NutBean meta, Object test) {
        if (null != test) {
            WnMatch m = new AutoMatch(test);
            return m.match(meta);
        }
        // 默认，没有的话永远为真，因为用户不设置 test 条件么
        return true;
    }

    public boolean hasMatch() {
        return null != match;
    }

    public Pattern getMatchPattern() {
        if (null == match) {
            return null;
        }
        if (match instanceof CharSequence) {
            String str = match.toString();
            if (str.startsWith("^")) {
                return Regex.getPattern(str);
            }
        }
        return null;
    }

    public WnMatch getMatchObj() {
        if (null == match) {
            return null;
        }
        return new AutoMatch(this.match);
    }

    public Object getMatch() {
        return match;
    }

    public void setMatch(Object match) {
        this.match = match;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean hasTarget() {
        return null != target && target.isAva();
    }

    public ThingLinkKeyTarget getTarget() {
        return target;
    }

    public void setTarget(ThingLinkKeyTarget target) {
        this.target = target;
    }

    public boolean hasVars() {
        return null != vars && !vars.isEmpty();
    }

    public NutMap getVars() {
        return vars;
    }

    public void setVars(NutMap vars) {
        this.vars = vars;
    }

    public boolean hasSet() {
        return null != set && set.size() > 0;
    }

    public NutMap getSet() {
        return set;
    }

    public void setSet(NutMap set) {
        this.set = set;
    }

    public boolean hasRun() {
        return null != run && run.length > 0;
    }

    public String[] getRun() {
        return run;
    }

    public void setRun(String[] run) {
        this.run = run;
    }

}
