package com.site0.walnut.ext.old.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.site0.walnut.api.io.WnObj;

public class BackupPackage {
    
    public WnObj self;

    public List<WobjLine> lines = new ArrayList<>();
    public Set<String> sha1Set = new TreeSet<>();
    public List<WnObj> objs = new ArrayList<>();

}
