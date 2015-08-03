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
    public static WnBucketBlockInfo getInfo(byte[] block) {
        WnBucketBlockInfo bi = new WnBucketBlockInfo();

        // 左边距
        for (; bi.paddingLeft < block.length; bi.paddingLeft++)
            if (block[bi.paddingLeft] != 0)
                break;

        // 右边距
        for (; bi.paddingRight >= bi.paddingLeft; bi.paddingRight++)
            if (block[block.length - bi.paddingRight - 1] != 0)
                break;

        // 有效数据长度
        bi.size = block.length - bi.paddingLeft - bi.paddingRight;

        // 搞定返回
        return bi;
    }

}
