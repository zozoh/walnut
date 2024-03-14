package com.site0.walnut.util.archive.impl;

import com.site0.walnut.util.archive.WnArchiveReading;
import com.site0.walnut.util.archive.WnArchiveReadingCallback;

public abstract class AsbatractWnAchiveRading implements WnArchiveReading {

    protected WnArchiveReadingCallback callback;

    @Override
    public void onNext(WnArchiveReadingCallback callback) {
        this.callback = callback;
    }

}
