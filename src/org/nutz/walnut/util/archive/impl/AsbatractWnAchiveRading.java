package org.nutz.walnut.util.archive.impl;

import org.nutz.walnut.util.archive.WnArchiveReading;
import org.nutz.walnut.util.archive.WnArchiveReadingCallback;

public abstract class AsbatractWnAchiveRading implements WnArchiveReading {

    protected WnArchiveReadingCallback callback;

    @Override
    public void onNext(WnArchiveReadingCallback callback) {
        this.callback = callback;
    }

}
