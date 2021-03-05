package org.nutz.walnut.util.range;

import java.io.IOException;
import java.io.InputStream;

public interface WnGetInputStream {

    InputStream getStream(long offset) throws IOException;

    long getContentLenth();

    String getETag();

}
