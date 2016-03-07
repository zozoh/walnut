package org.nutz.walnut.ext.hmaker.util;

import java.util.HashSet;
import java.util.Set;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

public class HmContext {

    public HComFactory coms;

    public WnIo io;

    public WnObj oHome;

    public WnObj oDest;

    public Set<WnObj> resources;

    public HmContext() {
        this.resources = new HashSet<WnObj>();
    }

    public HmContext(HmContext hpc) {
        this();
        this.coms = hpc.coms;
        this.io = hpc.io;
        this.oHome = hpc.oHome;
        this.oDest = hpc.oDest;
    }

}
