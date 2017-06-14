package org.nutz.walnut.ext.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BackupPackage {

    public List<WobjLine> lines = new ArrayList<>();
    public Set<String> sha1Set = new TreeSet<>();

}
