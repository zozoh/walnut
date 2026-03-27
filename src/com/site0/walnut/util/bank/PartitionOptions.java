package com.site0.walnut.util.bank;

public class PartitionOptions {

    /**
     * 每段有多长
     */
    public int width;
    /**
     * 分隔字符串
     */
    public String sep;
    /**
     * 分隔的方向：
     *
     * <ul>
     * <li>`left` 从右向左分隔，通常用来格式化金额 \
     * <li>`right` 从左至右分隔，通常用来格式化银行账号，或者软件激活码
     * <ul>
     */
    public HDirecton to;

    /**
     * 最多显示到小数点后几位
     */
    public int decimalPlaces;
    /**
     * 如果指定了 decimalPlaces，默认的则是自动不补零 如果指定了这个位数，后面需要补零
     */
    public Boolean decimalFixed;
}
