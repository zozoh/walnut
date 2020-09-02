package org.nutz.walnut.util.upload;

import java.io.IOException;

public interface HttpFormCallback {

    void handle(HttpFormField field) throws IOException;

}
