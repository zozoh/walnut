package org.nutz.walnut.util;

import org.nutz.walnut.api.io.WnBucketBlockInfo;

public abstract class Bus {

    /**
     * @see #evalInfo(byte[], WnBucketBlockInfo)
     */
    public static WnBucketBlockInfo getInfo(byte[] block) {
        WnBucketBlockInfo bi = new WnBucketBlockInfo();
        evalInfo(block, bi);
        return bi;
    }

    /**
     * @param block
     *            块的字节数组
     * @param bi
     *            输出块的布局信息
     * @return 块有效逻辑长度，即 paddingLeft + size
     */
    public static int evalInfo(byte[] block, WnBucketBlockInfo bi) {

        // 左边距
        int pl = 0;
        for (; pl < block.length; pl++)
            if (block[pl] != 0)
                break;

        // 右边距
        int pr = 0;
        for (; pr >= pl; pr++)
            if (block[block.length - pr - 1] != 0)
                break;

        // 有效数据长度
        int sz = block.length - pl - pr;

        // 输出信息
        if (null != bi)
            bi.set(pl, sz, pr);

        // 返回
        return pl + sz;
    }

}
