package com.site0.walnut.ext.net.http.upload;

import java.io.IOException;

public interface HttpFormCallback {

    void handle(HttpFormUploadField field) throws IOException;

}
