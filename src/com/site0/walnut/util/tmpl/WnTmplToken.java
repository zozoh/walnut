package com.site0.walnut.util.tmpl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import com.site0.walnut.alg.stack.WnCharStack;
import com.site0.walnut.alg.stack.WnStackPushResult;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.ele.TmplEle;
import com.site0.walnut.util.tmpl.ele.TmplStaticEle;
import com.site0.walnut.util.tmpl.segment.*;
import com.site0.walnut.util.tmpl.segment.TmplSegment;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;
import com.site0.walnut.util.validate.impl.NotMatch;

public class WnTmplToken {

    public static WnTmplToken[] parseToArray(String s) {
        return parseToArray(s.toCharArray());
    }

    public static WnTmplToken[] parseToArray(WnTmplTokenExpert expert, char[] cs) {
        List<WnTmplToken> list = parse(expert, cs);
        return list.toArray(new WnTmplToken[list.size()]);
    }

    public static WnTmplToken[] parseToArray(char[] cs) {
        List<WnTmplToken> list = parse(cs);
        return list.toArray(new WnTmplToken[list.size()]);
    }

    public static List<WnTmplToken> parse(String s) {
        return parse(s.toCharArray());
    }

    public static List<WnTmplToken> parse(char[] cs) {
        return parse(null, cs);
    }

    public static List<WnTmplToken> parse(WnTmplTokenExpert expert, char[] cs) {
        // 默认符号解析专家类
        if (null == expert) {
            expert = new WnTmplTokenExpert("$$", "${", '}');
        }

        // 准备符号结果列表
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
                        list.add(new WnTmplToken().asDynamic(s));
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

    private WnTmplTokenRace race;

    private WnTmplTokenType type;

    private String content;

    public String toString() {
        if (this.isRaceDynamic()) {
            return String.format("<%s#%s>: '%s'", race, type, content);
        }
        return String.format("<%s>: '%s'", race, content);
    }

    public WnTmplToken asDynamic(String content) {
        this.race = WnTmplTokenRace.DYNAMIC;
        this.content = content;
        return this.valueOf();
    }

    public WnTmplToken asText(String content) {
        this.race = WnTmplTokenRace.TEXT;
        this.content = content;
        return this.valueOf();
    }

    private static String REGEXP = "^#(if|else-if|else|end|loop)(.*)$";
    private static Pattern _P = Pattern.compile(REGEXP);

    public WnTmplToken valueOf() {
        // 动态符号，深入解析
        if (this.isRaceDynamic()) {
            String str = Ws.trim(content);
            Matcher m = _P.matcher(str);
            if (m.find()) {
                String stype = m.group(1);
                String st = Ws.snakeCase(stype).toUpperCase();
                this.type = WnTmplTokenType.valueOf(st);
                this.content = Ws.trim(m.group(2));
            }
            // 那就是普通占位符咯
            else {
                this.type = WnTmplTokenType.VAR;
                this.content = str;
            }
        }

        return this;
    }

    public TmplSegment createSegment(WnTmplElementMaker tknMaker) {
        // 动态符号
        if (this.isRaceDynamic()) {
            // #if | #else-if
            if (this.isTypeIf() || this.isTypeElseIf()) {
                ConditionTmplSegment sg_if = new ConditionTmplSegment();
                WnMatch wm = this.genMatchByContent();
                sg_if.setMatch(wm);
                return sg_if;
            }
            // #else
            if (this.isTypeElse()) {
                ConditionTmplSegment sg_if = new ConditionTmplSegment();
                return sg_if;
            }
            // #loop
            if (this.isTypeLoop()) {
                LoopTmplSegment sg_loop = new LoopTmplSegment();
                sg_loop.valueOf(content);
                return sg_loop;
            }

        }

        // 静态文本或者动态变量： 创建一个普通块
        BlockTmplSegment block = new BlockTmplSegment();
        TmplEle ele = this.createElement(tknMaker);
        block.addElement(ele);
        return block;
    }

    public TmplEle createElement(WnTmplElementMaker tknMaker) {
        // 静态文本
        if (this.isRaceText()) {
            return new TmplStaticEle(this.content);
        }
        // 动态变量
        if (this.isTypeVar()) {
            if (null != tknMaker) {
                TmplEle ele = tknMaker.make(this.content);
                if (null != ele) {
                    return ele;
                }
            }
            return WnTmpl.createTmplEle(this.content);
        }
        // 其他不能创建
        throw Wlang.impossible();
    }

    public WnMatch genMatchByContent() {
        String json = content;
        boolean not = false;
        if (json.startsWith("not")) {
            not = true;
            json = json.substring(3).trim();
        }

        if (!Ws.isQuoteBy(content, '[', ']') && !Ws.isQuoteBy(content, '{', '}')) {
            json = "{" + json + "}";
        }
        Object input = Json.fromJson(json);
        WnMatch wm = AutoMatch.parse(input);
        if (not) {
            wm = new NotMatch(wm);
        }
        return wm;

    }

    public boolean isRaceDynamic() {
        return WnTmplTokenRace.DYNAMIC == this.race;
    }

    public boolean isRaceText() {
        return WnTmplTokenRace.TEXT == this.race;
    }

    public WnTmplTokenRace getRace() {
        return race;
    }

    public void setRace(WnTmplTokenRace race) {
        this.race = race;
    }

    public boolean isTypeIf() {
        return WnTmplTokenType.IF == type;
    }

    public boolean isTypeElseIf() {
        return WnTmplTokenType.ELSE_IF == type;
    }

    public boolean isTypeElse() {
        return WnTmplTokenType.ELSE == type;
    }

    public boolean isTypeEnd() {
        return WnTmplTokenType.END == type;
    }

    public boolean isTypeVar() {
        return WnTmplTokenType.VAR == type;
    }

    public boolean isTypeLoop() {
        return WnTmplTokenType.LOOP == type;
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
