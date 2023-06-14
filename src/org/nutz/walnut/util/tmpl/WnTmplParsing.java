package org.nutz.walnut.util.tmpl;

import java.util.function.Predicate;

import org.nutz.walnut.alg.stack.LinkedStack;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.tmpl.ele.TmplEle;
import org.nutz.walnut.util.tmpl.segment.BlockTmplSegment;
import org.nutz.walnut.util.tmpl.segment.BranchTmplSegment;
import org.nutz.walnut.util.tmpl.segment.LoopTmplSegment;
import org.nutz.walnut.util.tmpl.segment.TmplSegment;

/**
 * <pre>
 * 
 * [Token]
 * [Token]
 * [Token]
 * [Token]  -->  [Segment]
 * [Token]  -->  [TmplX]
 * </pre>
 * 
 * @author zozoh
 *
 */
public class WnTmplParsing {

    private WnTmplToken[] tokens;

    private LinkedStack<TmplSegment> stack;

    private WnTmplX tmpl;

    public WnTmplParsing() {}

    public WnTmplX parse(char[] cs) {
        // 根对象
        this.tmpl = new WnTmplX();

        // 解析符号表
        this.tokens = WnTmplToken.parseToArray(cs);

        // 循环符号表
        stack = new LinkedStack<>();
        for (WnTmplToken t : this.tokens) {
            // #end: 弹出堆栈到 Branch 或者 loop，并压入栈顶对象
            // 如果栈顶元素不能接受子，则会弹出到可以接受子的对象，再压入
            // 如果栈空了，则直接计入根 tmp
            if (t.isTypeEnd()) {
                // 弹出到 Branch
                TmplSegment[] sgs = stack.popUtilAsArray(new Predicate<>() {
                    public boolean test(TmplSegment t) {
                        return (t instanceof BranchTmplSegment) || (t instanceof LoopTmplSegment);
                    }
                }, true, TmplSegment.class);

                // 确保最后一个一定是 Branch
                TmplSegment lastSeg = sgs[sgs.length - 1];
                if (!(lastSeg instanceof BranchTmplSegment)
                    && !(lastSeg instanceof LoopTmplSegment)) {
                    throw Er.create("e.tmpl.EndWithoutBeginLoopOfIf", t.toString());
                }

                // 从头开始向下合并
                margeDown(sgs);

                // 堆栈空了
                if (stack.isEmpty()) {
                    tmpl.addChild(lastSeg);
                }
                // 向下寻找第一个可以下压的对象
                else {
                    TmplSegment[] tops = stack.popUtilAsArray(new Predicate<>() {
                        public boolean test(TmplSegment t) {
                            return t.isCanAddChild();
                        }
                    }, true, TmplSegment.class);

                    // 从头开始向下合并
                    margeDown(tops);

                    // 最后一个元素还需要压回到栈顶
                    TmplSegment top = tops[tops.length - 1];
                    stack.push(top);
                }
            }
            // #if 压栈 Branch+Condition
            else if (t.isTypeIf()) {
                BranchTmplSegment br = new BranchTmplSegment();
                stack.push(br);
                TmplSegment sg = t.createSegment();
                stack.push(sg);
            }
            // #else-if/else 检查栈顶的条件为 Branch+Condition
            // 将 Condition 合并后，压入一个 新的Condition
            else if (t.isTypeElseIf() || t.isTypeElse()) {
                // 弹出到 Branch
                TmplSegment[] sgs = stack.popUtilAsArray(new Predicate<>() {
                    public boolean test(TmplSegment t) {
                        return (t instanceof BranchTmplSegment);
                    }
                }, true, TmplSegment.class);

                // 确保最后一个一定是 Branch
                TmplSegment lastSeg = sgs[sgs.length - 1];
                if (!(lastSeg instanceof BranchTmplSegment)) {
                    throw Er.create("e.tmpl.ElseWithoutIf", t.toString());
                }

                // 从头开始向下合并
                margeDown(sgs);

                // 将最后一个 Segment 也就是 Branch 再压回堆栈
                stack.push(lastSeg);

                // 创建当前的条件
                TmplSegment sg = t.createSegment();
                stack.push(sg);
            }
            // #loop 将会压栈 Loop
            else if (t.isTypeLoop()) {
                TmplSegment sg = t.createSegment();
                stack.push(sg);
            }
            // 其他则肯定是静态文本或者占位符，作为元素压入栈顶
            // 如果栈顶元素不能接受元素，则创建一个 Block
            else {
                TmplSegment top = stack.peek();
                if (null == top || !top.isCanAcceptElement()) {
                    BlockTmplSegment block = new BlockTmplSegment();
                    stack.push(block);
                    top = block;
                }
                TmplEle ele = t.createElement();
                top.addElement(ele);

            }
        }

        // 如果堆栈里还有东西也一并弹出
        if (!stack.isEmpty()) {
            TmplSegment[] list = stack.popAllAsArray(TmplSegment.class);
            // 从头开始向下合并
            margeDown(list);
            // 最后计入根
            TmplSegment sg = list[list.length - 1];
            tmpl.addChild(sg);
        }

        // 搞定
        return this.tmpl;
    }

    private void margeDown(TmplSegment[] sgs) {
        // 倒序合并
        int lastI = sgs.length - 1;
        TmplSegment current = sgs[lastI];
        for (int i = lastI - 1; i >= 0; i--) {
            TmplSegment sg = sgs[i];
            current.addChild(sg);
            if (sg.isCanAddChild()) {
                current = sg;
            }
        }
    }

}
