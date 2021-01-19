package org.nutz.walnut.ext.titanium.builder.bean;

import java.util.Map;

public class TiBuildConfig {

    private TiBuildEntry[] entries;

    private Map<String, TiBuildTarget> targets;

    public TiBuildEntry[] getEntries() {
        return entries;
    }

    public void setEntries(TiBuildEntry[] coms) {
        this.entries = coms;
    }

    public Map<String, TiBuildTarget> getTargets() {
        return targets;
    }

    public void setTargets(Map<String, TiBuildTarget> targets) {
        this.targets = targets;
    }

}
