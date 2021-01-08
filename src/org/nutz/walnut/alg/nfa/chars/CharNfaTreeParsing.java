package org.nutz.walnut.alg.nfa.chars;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.walnut.alg.ds.buf.WnCharArray;
import org.nutz.walnut.alg.ds.buf.WnLinkedArrayList;

class CharNfaTreeParsing {

    WnCharArray input;

    int lineNumber;

    WnLinkedArrayList<CharNfaComboNode> stack;

    CnnSeries currentNode;

    CharNfaTreeParsing() {
        stack = new WnLinkedArrayList<>(CharNfaComboNode.class, 10);
        currentNode = new CnnSeries();
    }

    void doParse() {
        char c;
        while ((c = input.nextChar()) != 0) {
            // 逃逸字符
            if ('\\' == c) {
                c = do_escape_char();
            }

            // 逐个字符判断
            switch (c) {
            
            // 进入组节点
            case '(':
                do_group();
                break;
                
            // 字符区间

            // 字符节点

            // 边数： *

            // 边数： ?

            // 边数： {n,n}

            // 普通字符

            }
        }
    }

    private void do_group() {
        
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
