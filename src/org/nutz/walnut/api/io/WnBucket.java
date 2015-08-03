package org.nutz.walnut.api.io;

public interface WnBucket {

    String getId();

    boolean isSealed();

    long getCreateTime();

    long getLastModified();

    long getLastReaded();

    long getLastWrited();

    long getLastSealed();

    long getLastOpened();

    long getCountRefer();

    long getCountRead();

    int getBlockSize();

    long getBlockNumber();

    String getFromBucketId();

    void setFromBucketId(String buid);

    boolean isDuplicated();

    long getSize();

    void setSize(long size);

    String getSha1();

    String getString();

    void write(String s);

    long write(long pos, byte[] bs, int off, int len);

    /**
     * 读取一个桶块
     * 
     * @param index
     *            桶块的下标，0 Base
     * @param bs
     *            字节数组，长度必须不能小于 block_size，桶块的字节将全部输出到这个数组
     * @param bi
     *            【选】输出的字节数组的具体布局
     * 
     * @return 输出了多少有效字节
     * 
     * @throws "e.bucket.OutputRange"
     *             桶块下标越界
     * 
     * @see WnBucketBlockInfo
     */
    int read(long index, byte[] bs, WnBucketBlockInfo bi);

    /**
     * 从桶中读取一些字节
     * 
     * @param pos
     *            开始读取的字节位置
     * @param bs
     *            输出的字节数组
     * @param off
     *            从输出的字节数组哪里开始写
     * @param len
     *            最多写多少
     * @return 实际读取了多少有效字节
     */
    int read(long pos, byte[] bs, int off, int len);

    /**
     * 写入一个桶块。如果桶块空间还有富余，则用空白填充
     * 
     * @param index
     *            桶块的下标
     * @param padding
     *            桶块开始的空白填充
     * @param bs
     *            字节数组，里面的字节会被写入桶块
     * @param off
     *            从字节数组什么地方开始写
     * @param len
     *            写多长
     */
    void write(long index, int padding, byte[] bs, int off, int len);

    /**
     * 忠实的安装给定的字节填充桶块
     * 
     * @param index
     *            桶块的下标
     * @param bs
     *            字节数组，里面的字节会被写入桶块，长度等于桶块。
     */
    void write(long index, byte[] bs);

    /**
     * 剪裁桶的有效数据大小
     * 
     * @param nb
     *            将桶块裁剪到多少个
     */
    void trancate(long nb);

    String seal();

    void unseal();

    /**
     * 复制出一个新桶
     * 
     * @param dropData
     *            复制的时候是否丢掉旧桶的数据
     * 
     * @return 新桶
     */
    WnBucket duplicate(boolean dropData);

    /**
     * 将另外一个桶的数据合并到当前的桶，本桶的 sha1 等字段会发生响应的改变。
     * <ul>
     * <li>如果原桶的桶块可以与本桶大小不一致
     * <li>本桶的填充字节会被原桶对应的自己替代
     * <ul>
     * 
     * @param bucket
     *            原桶
     * 
     * @return 自身以便链式赋值
     */
    WnBucket margeWith(WnBucket bucket);

    /**
     * 引用一个桶，返回引用后的计数
     * 
     * @return 引用计数
     */
    long refer();

    /**
     * @return 释放后，桶的引用计数，0 表示这个桶的数据将会被释放
     */
    long free();

}