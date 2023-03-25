package org.nutz.walnut.ext.data.thing.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.each.WnEachIteratee;
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

    public ThingLinkKey() {}

    public ThingLinkKey(Map<String, Object> map) {
        NutMap bean = NutMap.WRAP(map);
        this.testPrimary = bean.get("testPrimary");
        this.testUpdate = bean.get("testUpdate");
        this.match = bean.get("match");
        this.strict = bean.getBoolean("strict");
        NutMap ta = bean.getAs("target", NutMap.class);
        this.target = null == ta ? null : new ThingLinkKeyTarget(ta);
        this.vars = bean.getAs("vars", NutMap.class);
        this.set = bean.getAs("set", NutMap.class);
        Object run = bean.get("run");
        if (null != run) {
            List<String> runs = new LinkedList<>();
            Wlang.each(run, new WnEachIteratee<Object>() {
                public void invoke(int index, Object ele, Object src) {
                    if (null != ele) {
                        runs.add(ele.toString());
                    }
                }
            });
            this.run = runs.toArray(new String[runs.size()]);
        }
    }

    public boolean isDoNothing() {
        return !this.hasSet() && !this.hasRun();
    }

    public Object getTestPrimary() {
        return testPrimary;
    }

    public void setTestPrimary(NutMap[] testPrimary) {
        this.testPrimary = testPrimary;
    }

    private WnMatch __test_primary;

    public boolean matchTestPrimary(NutBean meta) {
        if (null == __test_primary) {
            __test_primary = AutoMatch.parse(testPrimary, true);
        }
        return __test_primary.match(meta);
    }

    public Object getTestUpdate() {
        return testUpdate;
    }

    public void setTestUpdate(NutMap[] testUpdate) {
        this.testUpdate = testUpdate;
    }

    private WnMatch __test_update;

    public boolean matchTestUpdate(NutBean meta) {
        if (null == __test_update) {
            __test_update = AutoMatch.parse(testUpdate, true);
        }
        return __test_update.match(meta);
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
