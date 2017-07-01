package org.nutz.walnut.ext.backup;

import org.nutz.lang.Strings;

public class WobjLine {
    public String id;
    public String path;
    public String obj_sha1;
    public String fdata_sha1;

    public WobjLine() {}
    
    

    public WobjLine(String id, String path, String obj_sha1, String fdata_sha1) {
        super();
        this.id = id;
        this.path = path;
        this.obj_sha1 = obj_sha1;
        this.fdata_sha1 = fdata_sha1;
    }



    public WobjLine(String line) {
        String[] tmp = line.split("[\\:]");
        id = tmp[0];
        path = tmp[1];
        obj_sha1 = tmp[2];
        if (tmp.length > 3)
            fdata_sha1 = tmp[3];
    }
    
    public String toString() {
        return String.format("%s:%s:%s:%s",
                             id,
                             path,
                             obj_sha1,
                             Strings.sBlank(fdata_sha1));
    }
}