package org.nutz.walnut.alg.nfa.chars;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.alg.ds.buf.WnCharArray;
import org.nutz.walnut.alg.ds.buf.WnLinkedArrayList;

class CharNfaTreeParsing {

    WnCharArray input;

    int lineNumber;

    /**
     * 解析的节点堆栈
     */
    WnLinkedArrayList<CharNfaNode> stack;

    /**
     * 栈顶节点指针
     */
    CharNfaNode topNode;

    CharNfaTreeParsing() {
        stack = new WnLinkedArrayList<>(CharNfaNode.class, 10);
        topNode = null;
    }

    void doParse() {
        char c;
        while ((c = input.nextChar()) != 0) {
            CharNfaNode node = null;

            // 逃逸字符
            if ('\\' == c) {
                c = do_escape_char();
                node = new CnnChar(c);
            }
            // 进入组节点: 需要压栈
            else if ('(' == c) {
                topNode = new CnnGroup();
                stack.add(topNode);
                continue;
            }
            // 离开组节点，需要弹出堆栈
            else if (')' == c) {
                // 确保栈顶为组
                if (topNode instanceof CnnGroup) {
                    CnnGroup cg = (CnnGroup) topNode;
                    // 关闭了组，那么它的行为就应该和普通节点一致了
                    cg.closed = true;
                    // 如果堆栈高度大于 1，则表示可以与上层节点连接
                    if (stack.size() > 1) {
                        node = stack.popLast();
                        topNode = stack.last();
                    }
                }
                // 否则不能容忍
                else {
                    throw Lang.makeThrow("unexpact char '%s' at %d", c, input.getPosition());
                }
            }
            // 并联操操作符: 需要确保当前栈顶为一个并联节点
            else if('|' == c) {
                if(null == topNode) {
                    throw Lang.makeThrow("unexpact char '%s' at %d", c, input.getPosition());
                }
                // 已经为并联节点了，
                if(topNode instanceof CnnParallel) {
                    
                }
            }
            // 逐个字符判断
            else {
                switch (c) {
                // 字符区间
                case '[':
                    node = do_char_range();
                    break;

                // 边数： *
                case '*':
                    if (null == topNode) {
                        throw Lang.makeThrow("unexpact char '%s' at %d", c, input.getPosition());
                    }
                    topNode.minMatch = 0;
                    topNode.maxMatch = 0;
                    break;

                // 边数： ?
                case '?':
                    if (null == topNode) {
                        throw Lang.makeThrow("unexpact char '%s' at %d", c, input.getPosition());
                    }
                    topNode.minMatch = 0;
                    topNode.maxMatch = 1;
                    break;

                // 边数： {n,n}
                case '{':
                    do_node_edge_number();
                    break;

                // 普通字符
                default:
                    node = new CnnChar(c);
                    break;
                }
            }

            // 不需要处理节点
            if (null == node)
                continue;

            // 合并节点: 如果没有节点，初始化栈
            if (null == topNode) {
                topNode = node;
                stack.add(node);
            }
            // 合并节点：与栈顶融合
            else {
                CharNfaNode node2 = topNode.joinWith(node);
                // 变成了其他节点，则改变一下堆栈栈顶
                if (node2 != topNode) {
                    topNode = node2;
                    stack.setLast(node2);
                }
            }
        }
    }

    private void do_node_edge_number() {
        if (null == topNode) {
            throw Lang.makeThrow("unexpact char '{' at %d", input.getPosition());
        }
        String s = input.nextString('}');
        String[] ss = Strings.splitIgnoreBlank(s);
        // 一个
        if (ss.length == 1) {
            int n = Integer.parseInt(ss[0]);
            topNode.minMatch = n;
            topNode.maxMatch = n;
        }
        // 两个
        else if (ss.length == 2) {
            int n0 = Integer.parseInt(ss[0]);
            int n1 = Integer.parseInt(ss[1]);
            topNode.minMatch = n0;
            topNode.maxMatch = n1;
        }
        // 其他通通错误
        else {
            throw Lang.makeThrow("unexpact '{%s' at %d", s, input.getPosition() - s.length() - 1);
        }
    }

    private CnnCharRange do_char_range() {
        return null;
    }

    private char do_escape_char() {
        char c;
        c = input.nextChar();
        switch (c) {
        case 'r':
            c = '\r';
            break;
        case 'n':
            c = '\n';
            break;
        case 't':
            c = '\t';
            break;
        case '(':
        case ')':
        case '[':
        case ']':
        case '{':
        case '}':
        case '*':
        case '?':
        case '\\':
            break;
        default:
            throw Lang.makeThrow("Invalid escape char [%s]", c);
        }
        return c;
    }

}
