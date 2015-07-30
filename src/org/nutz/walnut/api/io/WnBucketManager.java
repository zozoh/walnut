package org.nutz.walnut.api.io;

public interface WnBucketManager {

    WnBucket alloc(int blockSize);

    WnBucket getById(String buid);

    WnBucket checkById(String buid);

    WnBucket getBySha1(String sha1);

    WnBucket checkBySha1(String sha1);

}
