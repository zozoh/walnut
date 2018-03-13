package org.nutz.walnut.ext.hmaker.util.bean;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutMap;

public class HmcDynamicScriptInfo {

    public boolean loadRequest;

    public NutMap update;

    public List<NutMap> appends;

    public HmcDynamicScriptInfo() {
        this.update = new NutMap();
        this.appends = new LinkedList<NutMap>();
    }

}
