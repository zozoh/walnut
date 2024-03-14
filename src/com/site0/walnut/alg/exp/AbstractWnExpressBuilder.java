package com.site0.walnut.alg.exp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;

/**
 * 封装一个简单表达式的实现
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractWnExpressBuilder {

    /**
     * 运算符优先表，每个运算符，必须是一个 char
     */
    protected WnCharOpTable opTable;

    /**
     * 将表达式中的运算符，映射为优先表里的运算符
     */
    protected Map<String, Character> opMapping;

    /**
     * 符号解析的正则表达式，匹配的都是操作符
     */
    protected Pattern regex;

    public WnExpression parse(String input) {
        // 解析出来符号表
        int lastMatch = 0;
        Matcher m = regex.matcher(input);
        List<String> tokens = new LinkedList<>();
        while (m.find()) {
            int pos = m.start();
            if (pos > lastMatch) {
                tokens.add(input.substring(lastMatch, pos));
            }
            lastMatch = m.end();
            tokens.add(input.substring(pos, lastMatch));
        }

        // 子类根据自己的需求，预处理符号表
        tokens = prepareTokens(tokens);

        List<WnExpItem> items = new ArrayList<>(tokens.size());

        // 根据符号表依次处理
        for (String token : tokens) {
            // 操作符
            Character c = opMapping.get(token);
            if (null != c) {
                char op = c;
                int pri = opTable.getPriority(op);
                if (pri < 0) {
                    throw Lang.makeThrow("Invalid operator '%s'", token);
                }
                WnExpOperator ope = this.createOperator(token, op, pri);
                items.add(ope);
            }
            // 操作数
            else {
                WnExpValue val = this.createOperand(token);
                items.add(val);
            }
        }

        // 搞定
        return new WnExpression(items);
    }

    protected abstract WnExpOperator createOperator(String token, char op, int priority);

    protected abstract WnExpValue createOperand(String token);

    protected abstract List<String> prepareTokens(List<String> tokens);
}
