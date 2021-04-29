package org.nutz.walnut.ext.net.http;

import org.nutz.walnut.ext.net.http.bean.WnInputStreamInfo;

public interface HttpPathInputStreamFactory {

    WnInputStreamInfo getStreamInfo(String path);

}
