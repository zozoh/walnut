package org.nutz.walnut.alg.stack;

public enum WnStackPushResult {

    /**
     * 我不能接受这个字符
     */
    REJECT,

    /**
     * 我接受这个字符，并且我还可以接受更多
     */
    ACCEPT,

    /**
     * 我已经解析完成，不能接受更多内容了，清获取分析结果
     */
    DONE
}
