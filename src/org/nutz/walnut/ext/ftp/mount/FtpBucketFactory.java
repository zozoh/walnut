package org.nutz.walnut.ext.ftp.mount;

import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketFactory;
import org.nutz.walnut.api.io.WnObj;

public class FtpBucketFactory implements WnBucketFactory {

    public WnBucket getBucket(WnObj o) {
        return new FtpBucket(null);
    }

}
