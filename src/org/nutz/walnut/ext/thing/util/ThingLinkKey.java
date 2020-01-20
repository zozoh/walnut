package org.nutz.walnut.ext.thing.util;

import java.util.regex.Pattern;

import org.nutz.lang.util.NutMap;

public class ThingLinkKey {

    private Pattern match;

    private boolean strict;

    private ThingLinkKeyTarget target;

    private NutMap set;

    private String[] run;

    public boolean isDoNothing() {
        return !this.hasSet() && !this.hasRun();
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
