package org.nutz.walnut.ext.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.nutz.walnut.api.io.WnObj;

public class BackupPackage {
    
    public WnObj self;

    public List<WobjLine> lines = new ArrayList<>();
    public Set<String> sha1Set = new TreeSet<>();
    public List<WnObj> objs = new ArrayList<>();

}
