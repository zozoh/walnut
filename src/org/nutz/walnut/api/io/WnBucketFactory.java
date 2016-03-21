package org.nutz.walnut.api.io;

/**
 * 根据一个 WnObj 构建一个对象存储的桶实例
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnBucketFactory {

    /**
     * 为一个 WnObj构建一个桶实例。
     * <p>
     * 当然，如果你的实现不认为对你来说这是一个合法的 WnObj 请抛错 (Er.create(xxx")) <br>
     * 如果你返回的是 null 则表示你想让其他的人继续处理
     * 
     * @param o
     *            对象
     * @return 桶实例
     * @throws "e.bucket.o.invalid"
     *             - 传入的对象不合要求不能构建桶实例
     */
    WnBucket getBucket(WnObj o);

}
