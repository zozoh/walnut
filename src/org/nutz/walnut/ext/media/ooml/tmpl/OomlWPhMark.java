package org.nutz.walnut.ext.media.ooml.tmpl;

public class OomlWPhMark {

    public int index;

    public int offset;

    public OomlWPhMark() {}

    public OomlWPhMark update(int[] offsets, int pos) {
        for (int i = 0; i < offsets.length; i++) {
            int off = offsets[i];
            // 考虑到每个段落，并一定是开始就是占位符起始标记
            // 找到第一个大于指定唯一的下标，那么就是其前一个位置为占位符标记的所在项
            if (off > pos) {
                this.index = Math.max(0, i - 1);
                this.offset = pos - offsets[this.index];
                break;
            }
        }
        return this;
    }

    public String toString() {
        return String.format("[%d:%d]", index, offset);
    }

    public OomlWPhMark clone() {
        OomlWPhMark mk = new OomlWPhMark();
        mk.index = this.index;
        mk.offset = this.offset;
        return mk;
    }
}
