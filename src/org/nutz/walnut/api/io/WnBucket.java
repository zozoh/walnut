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

    int getBlockNumber();

    String getParentBucketId();

    void setParentBucketId(String pbid);

    boolean isDuplicated();

    long getSize();

    void setSize(long size);

    String getSha1();

    String getString();

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
    int read(int index, byte[] bs, WnBucketBlockInfo bi);

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
     *            从桶块哪个位置开始写
     * @param bs
     *            字节数组，里面的字节会被写入桶块
     * @param off
     *            从字节数组什么地方开始写
     * @param len
     *            写多长
     * @return 实际写入的字节数
     */
    int write(int index, int padding, byte[] bs, int off, int len);

    int write(String s);

    int append(String s);

    int write(long pos, byte[] bs, int off, int len);

    int append(byte[] bs, int off, int len);

    /**
     * 剪裁桶的有效数据大小
     * 
     * @param nb
     *            将桶块裁剪到多少个
     */
    void trancate(int nb);

    String seal();

    void unseal();

    /**
     * 更新桶的状态。如果桶是分布式实现的，这是个同步桶元数据的好时机
     */
    void update();

    /**
     * 复制出一个新的桶，逻辑上是接着本桶的，但是里面没有数据 <br>
     * 本桶的最后一块如果没满，也将被复制<br>
     * 数据读取时，会复制的桶会连接本桶读取
     * 
     * @return 新的桶
     */
    WnBucket duplicateVirtual();

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