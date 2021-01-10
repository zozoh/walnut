package org.nutz.walnut.alg.rpn;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;

/**
 * 将中缀表达式变成一个逆波特兰式的抽象过程， 这个过程有两种参与者：
 * 
 * <ul>
 * <li>操作数 : value
 * <li>操作符 : operator
 * <li>分组符 : groupBegin / groupEnd
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class RpnParsing {

    /**
     * 存放操作符
     */
    private LinkedList<RpnOperator> stack;

    /**
     * 存操作数和操作符
     */
    private LinkedList<RpnItem> result;

    /**
     * 为了调试便利，存储每次扫描的步进结果
     */
    private LinkedList<RpnItem> items;

    public List<RpnItem> parse(Collection<RpnItem> coll) {
        return parse(coll.iterator());
    }

    public List<RpnItem> parse(Iterator<RpnItem> itera) {
        return parse(itera);
    }

    public List<RpnItem> parse(Iterator<RpnItem> itera, Callback<RpnParsing> watcher) {
        // 初始化两个栈：运算符栈s1和储存中间结果的栈s2
        this.stack = new LinkedList<>();
        this.result = new LinkedList<>();
        if (null != watcher) {
            items = new LinkedList<>();
        }

        // 从左至右扫描中缀表达式
        while (itera.hasNext()) {
            RpnItem it = itera.next();
            if (null != items) {
                items.add(it);
            }

            // 遇到操作数时，将其存入结果栈
            if (it instanceof RpnValue) {
                result.add(it);
            }
            // 遇到运算符时 ...
            else if (it instanceof RpnOperator) {
                RpnOperator op = (RpnOperator) it;
                // 起始分组，或者操作栈为空
                if (stack.isEmpty() || (it instanceof RpnGroupBegin)) {
                    stack.addLast(op);
                }
                // 结束分组，那么就弹吧
                else if (it instanceof RpnGroupEnd) {
                    // 则将 stack 栈中 ( 前的所有运算符出栈，存入结果栈。
                    while (!stack.isEmpty()) {
                        RpnOperator op2 = stack.removeLast();
                        if (op2 instanceof RpnGroupBegin) {
                            break;
                        }
                        result.addLast(op2);
                    }
                }
                // 若该运算符为非括号，则将该运算符和 stack 栈顶运算符作比较：
                // - 若高于栈顶运算符，则直接存入 stack 栈，
                else if (op.isHigherPriority(stack.getLast())) {
                    stack.addLast(op);
                }
                // - 否则将栈顶运算符出栈，并入结果栈
                // 直到遇到发现更低优先级的元素(或者栈为空)为止
                else {
                    RpnOperator op2 = stack.removeLast();
                    result.addLast(op2);
                    while (!stack.isEmpty() && !op.isHigherPriority(stack.getLast())) {
                        op2 = stack.removeLast();
                        result.addLast(op2);
                    }
                    stack.addLast(op);
                }
            }
            // 神马玩意？ 不认识！抛错
            else {
                throw Lang.makeThrow("Invalid RPN Item: %s", it.toString());
            }

            // 回调观察者
            if (null != watcher) {
                watcher.invoke(this);
            }
        }

        // 扫描完成，清空操作栈
        while (!stack.isEmpty()) {
            RpnOperator op = stack.removeLast();
            result.addLast(op);
        }
        if (null != watcher) {
            watcher.invoke(this);
        }

        // 搞定
        return result;
    }

}
