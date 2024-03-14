package com.site0.walnut.util.callback;

/**
 * 字符串符号回调对象
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public enum WnStrTokenType {

    /**
     * 被引号包裹的字符串
     * 
     * <pre>
     * index : 23,     // 指向当前字符的下标 
     * c     : '`'     // 包裹字符串的引号
     * text  : "xxx"   // 包裹的内容（不包括引号）
     * </pre>
     */
    QUOTE,

//    /**
//     * 连续遇到两个引号
//     * 
//     * <pre>
//     * index : 23,     // 指向当前字符的下标
//     * c     : '`'     // 引号字符
//     * text  : ""      // 内容就是一个空字符串
//     * </pre>
//     */
//    PAIR,

//    /**
//     * 遇到了 escape 字符
//     * 
//     * <pre>
//     * index : 23,     // 指向当前字符的下标
//     * c     : '`'     // 引号字符
//     * text  : ""      // 内容就是一个空字符串
//     * </pre>
//     */
//    ESCAPE,

    /**
     * 遇到了分隔符
     * 
     * <pre>
     * index : 23,     // 指向当前字符的下标
     * c     : ' '     // 分隔字符
     * text  : ""      // 内容就是一个空字符串
     * </pre>
     */
    SEPERATOR,

//    /**
//     * 遇到字符串结束，即最后一个字符为 '\'
//     * 
//     * <pre>
//     * index : 23,     // 指向当前字符的下标
//     * c     : '\\'    // 逃逸字符
//     * text  : ""      // 内容就是一个空字符串
//     * </pre>
//     */
//    JOIN,

    /**
     * 普通文本
     * 
     * <pre>
     * index : 23,     // 指向当前字符的下标
     * c     : 0       // 空字符 
     * text  : "xxx"   // 内容字符串
     * </pre>
     */
    TEXT

}
