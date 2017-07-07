package org.nutz.walnut.ext.qiniu.mount;

import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketFactory;
import org.nutz.walnut.api.io.WnObj;

public class QiniuBucketFactory implements WnBucketFactory {

    public WnBucket getBucket(WnObj obj) {
        return new QininBucket(new QiniuWnObj(obj));
    }

}
