package org.nutz.walnut.util.archive;

import java.io.IOException;
import java.io.InputStream;

public interface WnArchiveReadingCallback {

    void invoke(WnArchiveEntry en, InputStream ins) throws IOException;

}
