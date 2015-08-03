package org.nutz.walnut.util;

import java.util.Arrays;

import org.nutz.walnut.api.io.WnBucketBlockInfo;

public abstract class Bus {

    public static void fillBlock(byte[] block, int padding, byte[] bs, int off, int len) {
        // 填充左边距
        if (padding > 0)
            Arrays.fill(block, 0, padding, (byte) 0);

        // 填充数据
        System.arraycopy(bs, off, block, padding, len);

        // 填充右边距
        int paddingRight = block.length - padding - len;
        if (paddingRight > 0)
            Arrays.fill(block, padding + len, block.length, (byte) 0);
    }

    /**
     * @param block
     *            块的字节数组
     * @return 块的边距信息
     */
    public static int getInfo(byte[] block, WnBucketBlockInfo bi) {

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

        if (null != bi)
            bi.set(pl, sz, pr);

        // 搞定返回
        return sz;
    }

}
