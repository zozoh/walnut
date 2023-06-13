package org.nutz.walnut.util.tmpl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.alg.stack.WnCharStack;
import org.nutz.walnut.alg.stack.WnStackPushResult;

public class WnTmplToken {

    public static WnTmplToken[] parseToArray(String s) {
        return parseToArray(s.toCharArray());
    }

    public static WnTmplToken[] parseToArray(char[] cs) {
        List<WnTmplToken> list = parse(cs);
        return list.toArray(new WnTmplToken[list.size()]);
    }

    public static List<WnTmplToken> parse(String s) {
        return parse(s.toCharArray());
    }

    public static List<WnTmplToken> parse(char[] cs) {
        WnTmplTokenExpert expert = new WnTmplTokenExpert("$$", "${", '}');
        return parse(expert, cs);
    }

    public static List<WnTmplToken> parse(WnTmplTokenExpert expert, char[] cs) {
        List<WnTmplToken> list = new LinkedList<>();
        // 准备解析栈
        WnCharStack stack = expert.createCharStack();

        // 准备缓冲字符，以便匹配特征
        StringBuilder sb = new StringBuilder();
        // 逐个处理字符
        int n = cs.length;
        for (int i = 0; i < n; i++) {
            char c = cs[i];
            char c2 = expert.pushToBuf(c);
            // 普通字符
            if (c2 > 0) {
                sb.append(c2);
            }
            // 逃逸字符
            if (expert.isEscape()) {
                sb.append(expert.escapeBuf());
                expert.clearBuf();
            }
            // 启用解析堆栈
            else if (expert.isStarts()) {
                // 收集之前的符号
                if (sb.length() > 0) {
                    list.add(new WnTmplToken().asText(sb.toString()));
                    sb = new StringBuilder();
                }
                while (i < n) {
                    WnStackPushResult re = stack.push(c);
                    // 解析完毕
                    if (WnStackPushResult.DONE == re) {
                        String s = stack.getContentAndReset();
                        list.add(new WnTmplToken().asVar(s));
                        break;
                    }
                    i++;
                    if (i >= n) {
                        break;
                    }
                    c = cs[i];
                }
                // 缓冲没用了
                expert.clearBuf();
            }

        }
        // 收集Buffer
        expert.joinBufToString(sb);

        // 收集之前的符号
        if (sb.length() > 0) {
            list.add(new WnTmplToken().asText(sb.toString()));
        }
        return list;
    }

    private WnTmplTokenType type;

    private String content;

    public String toString() {
        return String.format("<%s>: '%s'", type, content);
    }

    public WnTmplToken asVar(String content) {
        this.type = WnTmplTokenType.VAR;
        this.content = content;
        return this;
    }

    public WnTmplToken asText(String content) {
        this.type = WnTmplTokenType.TEXT;
        this.content = content;
        return this;
    }

    public WnTmplTokenType getType() {
        return type;
    }

    public void setType(WnTmplTokenType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
