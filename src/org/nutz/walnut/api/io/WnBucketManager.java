package org.nutz.walnut.api.io;

import org.nutz.walnut.util.UnitTestable;

public interface WnBucketManager extends UnitTestable {

    WnBucket alloc(int blockSize);

    WnBucket getById(String buid);

    WnBucket checkById(String buid);

    WnBucket getBySha1(String sha1);

    WnBucket checkBySha1(String sha1);

    /**
     * 将冷却的未去重的桶进行去重操作
     * 
     * @param tree
     *            索引接口，这个操作可能会主动修改对象的索引
     * @param n
     *            最多处理多少个桶，小于等于0，表示不限制数量
     * @return 实际成功合并了多少个桶
     */
    int mergeSame(WnTree tree, int n);

}
