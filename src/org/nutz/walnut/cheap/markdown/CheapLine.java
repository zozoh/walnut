package org.nutz.walnut.cheap.markdown;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.util.Ws;

class CheapLine {

    /**
     * 行号（0 base）
     */
    int number;

    /**
     * 行类型
     */
    LineType type;

    ListType listType;

    CodeType codeType;

    /**
     * 多少个空格
     */
    int space;

    /**
     * 前缀
     * 
     * <ul>
     * <li>无序列表: 前缀字符 <code>+*-</code>
     * <li>有序列表: 序号起始值
     * </ul>
     */
    String prefix;

    /**
     * 级别
     * 
     * <ul>
     * <li>引用块: 嵌入级别
     * <li>标题行: 大纲级别
     * <li>列表行: 缩进级别
     * </ul>
     */
    int level;

    /**
     * 有序列表，开始数字
     */
    int startNumber;

    /**
     * 行原始内容
     */
    String rawData;

    /**
     * 行除去起始空白后的内容
     */
    String trimed;

    /**
     * 行的实际有效内容
     */
    String content;

    CheapLine(int number, String input) {
        this.number = number;
        this.rawData = input;
    }

    @Override
    public String toString() {
        String s0 = Ws.repeat(' ', space);
        String s1;
        // 无前缀
        if (null == prefix) {
            s1 = "";
        }
        // 列表
        else if (LineType.LIST == type) {
            s1 = prefix + " ";
        }
        // 其他前缀
        else {
            s1 = prefix;
        }
        return String.format("Line(%d) %s%s%s", number, s0, s1, content);
    }

    private static String REGEX = "^(\\s*)(" // Start: 1,2
                                  + "(-{3,})" // HR:3
                                  + "|(([+*-]) (.+))" // UL: 4,5,6
                                  + "|(((\\d+)\\.) (.+))" // OL: 7,8,9,10
                                  + "|(((>\\s*)+)(.+))" // QUOTE: 11,12,13,14
                                  + "|((`{3,})(.*))" // CODE:GFM: 15,16,17
                                  + "|((#+) (.*))" // HEADING: 18,19,20
                                  + "|([|:-]{3,})" // TABLE_HEAD: 21,
                                  + "|(.+)" // P: 22
                                  + ")(\\s*)$"; // End: 23

    private static Pattern P = Regex.getPattern(REGEX);

    /**
     * 根据内容自动判断类型
     */
    void evalType(String tab, int codeIndent) {
        Matcher m = P.matcher(rawData);
        // 空行
        if (!m.find()) {
            this.type = LineType.BLANK;
            return;
        }

        // 得到头部空格数量
        String sh = m.group(1).replace("\t", tab);
        this.space = sh.length();
        this.trimed = m.group(2) + m.group(23);

        // 分隔线
        if (null != m.group(3)) {
            this.type = LineType.HR;
            return;
        }
        // 无序列表
        if (null != m.group(4)) {
            this.type = LineType.LIST;
            this.listType = ListType.UL;
            this.prefix = m.group(5);
            this.content = m.group(6);
            return;
        }
        // 有序列表
        if (null != m.group(7)) {
            this.type = LineType.LIST;
            this.listType = ListType.OL;
            this.prefix = m.group(8);
            this.startNumber = Integer.parseInt(m.group(9));
            this.content = m.group(10);
            return;
        }
        // 引用块
        if (null != m.group(11)) {
            this.type = LineType.BLOCKQUOTE;
            this.prefix = m.group(12);
            this.level = Ws.countChar(prefix, '>');
            this.content = m.group(14);
            return;
        }
        // 代码块
        if (null != m.group(15)) {
            this.type = LineType.CODE_BLOCK;
            this.codeType = CodeType.GFM;
            this.prefix = m.group(16);
            this.content = Ws.trim(m.group(17));
            return;
        }
        // 标题
        if (null != m.group(18)) {
            this.type = LineType.HEADING;
            this.prefix = m.group(19);
            this.level = Ws.countChar(this.prefix, '#');
            this.content = Ws.trim(m.group(20));
            return;
        }
        // 表格分隔线
        if (null != m.group(21)) {
            this.type = LineType.TABKE_HEAD_LINE;
            return;
        }
        // 缩进代码块
        if (this.space >= codeIndent) {
            this.type = LineType.CODE_BLOCK;
            this.codeType = CodeType.INDENT;
            this.shiftSpace(codeIndent);
            this.content = this.trimed;
            return;
        }

        // 默认就算是段落
        this.content = m.group(22);
        if (Strings.isBlank(this.content)) {
            this.type = LineType.BLANK;
        } else {
            this.type = LineType.PARAGRAPH;
        }
    }

    void shiftSpace(int backSpace) {
        if (backSpace <= this.space) {
            this.space -= backSpace;
            this.trimed = Ws.repeat(' ', backSpace) + trimed;
        }
    }

}
