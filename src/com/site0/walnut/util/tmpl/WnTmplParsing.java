package com.site0.walnut.util.tmpl;

import java.util.function.Predicate;

import com.site0.walnut.alg.stack.LinkedStack;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.tmpl.ele.TmplEle;
import com.site0.walnut.util.tmpl.segment.BlockTmplSegment;
import com.site0.walnut.util.tmpl.segment.BranchTmplSegment;
import com.site0.walnut.util.tmpl.segment.LoopTmplSegment;
import com.site0.walnut.util.tmpl.segment.TmplSegment;

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

    private WnTmplElementMaker tknMaker;

    private WnTmplTokenExpert expert;

    private WnTmplToken[] tokens;

    private LinkedStack<TmplSegment> stack;

    private WnTmplX tmpl;

    public WnTmplParsing(WnTmplElementMaker tknMaker) {
        this.tknMaker = tknMaker;
    }

    public WnTmplX parse(char[] cs) {
        // 根对象
        this.tmpl = new WnTmplX();

        // 解析符号表
        this.tokens = WnTmplToken.parseToArray(expert, cs);

        // 循环符号表
        stack = new LinkedStack<>();
        for (WnTmplToken t : this.tokens) {
            // #end: 弹出堆栈到 Branch 或者 loop，并压入栈顶对象
            // 如果栈顶元素不能接受子，则会弹出到可以接受子的对象，再压入
            // 如果栈空了，则直接计入根 tmpl
            if (t.isTypeEnd()) {
                // 弹出到 Branch
                TmplSegment[] sgs = stack.popUtilAsArray(new Predicate<>() {
                    public boolean test(TmplSegment t) {
                        return (t instanceof BranchTmplSegment) || (t instanceof LoopTmplSegment);
                    }
                }, true, TmplSegment.class);

                // 确保最后一个一定是 Branch/Loop
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

                    // 为了防止
                    // 01) [ Loop ]
                    // 00) [ Block ]
                    // 这种堆栈，需要从后向前确认一个最终能接受子的对象
                    int lastI = tops.length - 1;
                    for (; lastI >= 0; lastI--) {
                        TmplSegment it = tops[lastI];
                        if (it.isCanAddChild()) {
                            break;
                        }
                    }

                    // 如果 lastI 不是最后一个，那么堆栈必然被弹空了，将这些元素统统压入根即可
                    for (int x = tops.length - 1; x > lastI; x--) {
                        TmplSegment it = tops[x];
                        tmpl.addChild(it);
                    }

                    // 从头开始向下合并
                    TmplSegment top = margeDown(tops, lastI);

                    // 将之前组合好的 lastSeg 压入最后一个元素，并将 top 要压回到栈顶
                    if (null != top) {
                        top.addChild(lastSeg);
                        stack.push(top);
                    }
                    // 那么就是根咯
                    else {
                        tmpl.addChild(lastSeg);
                    }
                }
            }
            // #if 压栈 Branch+Condition
            else if (t.isTypeIf()) {
                BranchTmplSegment br = new BranchTmplSegment();
                stack.push(br);
                TmplSegment sg = t.createSegment(this.tknMaker);
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
                TmplSegment sg = t.createSegment(this.tknMaker);
                stack.push(sg);
            }
            // #loop 将会压栈 Loop
            else if (t.isTypeLoop()) {
                TmplSegment sg = t.createSegment(this.tknMaker);
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
                TmplEle ele = t.createElement(this.tknMaker);
                top.addElement(ele);

            }
        }

        // 如果堆栈里还有东西也一并弹出
        if (!stack.isEmpty()) {
            TmplSegment[] list = stack.popAllAsArray(TmplSegment.class);

            // 看看栈底的对象，找到一个能计入子对象的，其他都先入根
            int lastI = list.length - 1;
            for (; lastI >= 0; lastI--) {
                TmplSegment it = list[lastI];
                tmpl.addChild(it);
            }

            // 从头开始向下合并
            TmplSegment top = margeDown(list, lastI);

            // 将之前组合好的 lastSeg 压入最后一个元素，并将 top 要压回到栈顶
            if (null != top) {
                tmpl.addChild(top);
            }

        }

        // 搞定
        return this.tmpl;
    }

    private TmplSegment margeDown(TmplSegment[] sgs) {
        // 倒序合并
        int lastI = sgs.length - 1;
        return margeDown(sgs, lastI);
    }

    private TmplSegment margeDown(TmplSegment[] sgs, int lastI) {
        if (lastI < 0 || lastI >= sgs.length) {
            return null;
        }
        // 倒序合并
        TmplSegment current = sgs[lastI];
        for (int i = lastI - 1; i >= 0; i--) {
            TmplSegment sg = sgs[i];
            current.addChild(sg);
            if (sg.isCanAddChild()) {
                current = sg;
            }
        }
        return sgs[lastI];
    }

    public WnTmplTokenExpert getExpert() {
        return expert;
    }

    public void setExpert(WnTmplTokenExpert expert) {
        this.expert = expert;
    }

}
