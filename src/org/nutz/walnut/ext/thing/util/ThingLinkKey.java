package org.nutz.walnut.ext.thing.util;

import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.validate.WnValidate;

public class ThingLinkKey {

    private NutMap[] testPrimary;

    private NutMap[] testUpdate;

    private Pattern match;

    private boolean strict;

    private ThingLinkKeyTarget target;

    private NutMap set;

    private String[] run;

    public boolean isDoNothing() {
        return !this.hasSet() && !this.hasRun();
    }

    public NutMap[] getTestPrimary() {
        return testPrimary;
    }

    public void setTestPrimary(NutMap[] testPrimary) {
        this.testPrimary = testPrimary;
    }

    public boolean matchTestPrimary(NutBean meta) {
        return matchTest(meta, testPrimary);
    }

    public NutMap[] getTestUpdate() {
        return testUpdate;
    }

    public void setTestUpdate(NutMap[] testUpdate) {
        this.testUpdate = testUpdate;
    }

    public boolean matchTestUpdate(NutBean meta) {
        return matchTest(meta, testUpdate);
    }

    private boolean matchTest(NutBean meta, NutMap[] tests) {
        if (null != tests && tests.length > 0) {
            for (NutMap vMap : tests) {
                WnValidate wv = new WnValidate(vMap);
                if (wv.match(meta)) {
                    return true;
                }
            }
            return false;
        }
        // 默认，没有的话永远为真，因为用户不设置 test 条件么
        return true;
    }

    public boolean hasMatch() {
        return null != match;
    }

    public Pattern getMatch() {
        return match;
    }

    public void setMatch(Pattern match) {
        this.match = match;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean hasTarget() {
        return null != target && (target.hasFilter() || target.hasThingSet());
    }

    public ThingLinkKeyTarget getTarget() {
        return target;
    }

    public void setTarget(ThingLinkKeyTarget target) {
        this.target = target;
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
