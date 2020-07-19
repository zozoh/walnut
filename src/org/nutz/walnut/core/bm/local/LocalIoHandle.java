package org.nutz.walnut.core.bm.local;

import java.io.File;
import org.nutz.walnut.core.WnIoHandle;

public abstract class LocalIoHandle extends WnIoHandle {

    protected LocalIoBM bm;

    protected File buck;

    LocalIoHandle(LocalIoBM bm) {
        this.bm = bm;
    }

    protected File getBuckFile() {
        if (null == buck) {
            buck = bm.getBucketFile(obj.data());
        }
        return buck;
    }
}
