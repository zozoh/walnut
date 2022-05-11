package org.nutz.walnut;

/**
 * 记录一下版本号
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public final class WnVersion {

    /**
     * @return 版本号
     */
    public static String get() {
        return "11.81";
    }

    /**
     * @return 版本代号
     */
    public static String alias() {
        return "TIGER";
    }

    /**
     * @return 一个完整的全描述的版本信息
     */
    public static String getName() {
        return String.format("Walnut%s(%s)", get(), alias());
    }

    // 禁止实例化
    private WnVersion() {}
}
