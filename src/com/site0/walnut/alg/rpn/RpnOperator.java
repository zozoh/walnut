package com.site0.walnut.alg.rpn;

public interface RpnOperator extends RpnItem {

    boolean isHigherPriority(RpnOperator op);

    boolean isLowerPriority(RpnOperator op);

    boolean isSamePriority(RpnOperator op);

    /**
     * 与另外一个操作符比较优先级
     * 
     * @param op
     *            另外的操作符
     * @return
     *         <ul>
     *         <li><code>0</code> : 优先级相等
     *         <li><code>小于0</code> : 自己优先级更低
     *         <li><code>大于0</code> : 自己优先级更高
     *         </ul>
     */
    int comparePriority(RpnOperator op);
    
    int getPriority();

}
