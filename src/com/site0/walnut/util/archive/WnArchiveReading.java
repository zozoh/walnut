package com.site0.walnut.util.archive;

import java.io.IOException;

public interface WnArchiveReading {

    void onNext(WnArchiveReadingCallback callback);

    int readAll() throws IOException;
}
