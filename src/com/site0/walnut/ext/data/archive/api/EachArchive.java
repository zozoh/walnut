package com.site0.walnut.ext.data.archive.api;

import java.io.IOException;
import java.io.InputStream;

public interface EachArchive {

    void invoke(int index, ArchiveEntry en, InputStream ins) throws IOException;

}
