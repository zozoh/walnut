package org.nutz.walnut.alg.exp;

import java.util.Arrays;

/**
 * 封装了一个字符型运算符优先级的表
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnCharOpTable {

    /**
     * 优先级表格
     * <p>
     * char[优先级:0-n][字符下标:index]
     */
    private char[][] table;

    /**
     * 速度优化表第一个字符起始值
     */
    private int charCodeOffset;

    /**
     * 根据 charCode 可以直接得到优先级的表
     */
    private int[] charPriorities;

    /**
     * 根据一个这样格式的字符串表达优先级
     * 
     * <pre>
     * "+-"   低
     * "/*"   V
     * "()"   高
     * </pre>
     * 
     * @param input
     *            输入的优先级表格
     */
    public WnCharOpTable(String... input) {
        // 第一遍扫描，获取一个二维表格，并得到字符的最大和最小取值
        int maxChar = Integer.MIN_VALUE;
        int minChar = Integer.MAX_VALUE;
        char[][] css = new char[input.length][];
        for (int i = 0; i < input.length; i++) {
            String s = input[i];
            char[] cs = s.toCharArray();
            css[i] = cs;
            for (char c : cs) {
                maxChar = Math.max(c, maxChar);
                minChar = Math.min(c, minChar);
            }
        }
        this.table = css;

        // 那么我们就可以构建一个数组
        this.charCodeOffset = minChar;
        int len = maxChar - minChar + 1;
        charPriorities = new int[len];
        Arrays.fill(charPriorities, -1);

        // 填充表
        for (int pri = 0; pri < table.length; pri++) {
            char[] cs = table[pri];
            for (int i = 0; i < cs.length; i++) {
                char c = cs[i];
                int ix = c - this.charCodeOffset;
                this.charPriorities[ix] = pri;
            }
        }
    }

    /**
     * 获取一个操作符的优先级
     * 
     * @param op
     *            操作符
     * @return 优先级。 -1 表示给定字符不在表内。 0 表示最低优先级，越大越高
     */
    public int getPriority(char op) {
        int ix = op - this.charCodeOffset;
        if (ix < 0 || ix >= this.charPriorities.length)
            return -1;

        return this.charPriorities[ix];
    }

    /**
     * 比较两个操作符的优先级
     * 
     * @param opA
     *            A 操作符
     * @param opB
     *            B 操作符
     * @return
     *         <ul>
     *         <li><code>0</code> : 优先级相等
     *         <li><code>小于0</code> : B 比 A 优先级高
     *         <li><code>大于0</code> : A 比 B 优先级高
     *         </ul>
     */
    public int compare(char opA, char opB) {
        int a = this.getPriority(opA);
        int b = this.getPriority(opB);
        return a - b;
    }

}
