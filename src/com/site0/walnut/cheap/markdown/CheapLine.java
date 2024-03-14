package com.site0.walnut.cheap.markdown;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.util.Regex;
import com.site0.walnut.util.Ws;

class CheapLine {

    /**
     * 行号（0 base）
     */
    int lineNumber;

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
    
    CheapLine(CheapLine line) {
        this.lineNumber = line.lineNumber;
        this.rawData = line.content;
    }

    CheapLine(int number, String input) {
        this.lineNumber = number;
        this.rawData = input;
    }

    @Override
    public String toString() {
        String s0 = null == this.type ? "???" : this.type.toString();
        if (null != this.codeType) {
            s0 += ":" + this.codeType;
        }
        if (null != this.listType) {
            s0 += ":" + this.listType;
        }
        String s1 = Ws.repeat(' ', space);
        String s2;
        // 无前缀
        if (null == prefix) {
            s2 = "";
        }
        // 列表和标题，都需要补一个空格
        else if (LineType.LIST == type || LineType.HEADING == type) {
            s2 = prefix + " ";
        }
        // 其他前缀
        else {
            s2 = prefix;
        }
        return String.format("Line(%d)<%s> %s%s%s", lineNumber, s0, s1, s2, content);
    }

    private static String REG_LNK_REF = "^\\s{0,3}\\[([^\\]]+)\\]:\\s*(.+)$";
    private static Pattern P_LNK_REF = Regex.getPattern(REG_LNK_REF);

    private static String REGEX = "^(\\s*)(" // Start: 1,2
                                  + "(-{3,})" // HR:3
                                  + "|(([+*-]) (.+))" // UL: 4,5,6
                                  + "|(((\\d+)\\.) (.+))" // OL: 7,8,9,10
                                  + "|(((>\\s?))(.*))" // QUOTE: 11,12,13,14
                                  + "|((`{3,})(.*))" // CODE:GFM: 15,16,17
                                  + "|((#+) (.*))" // HEADING: 18,19,20
                                  + "|([|:-]{3,})" // TABLE_HEAD: 21,
                                  + "|(.+)" // P: 22
                                  + ")(\\s*)$"; // End: 23

    private static Pattern P = Regex.getPattern(REGEX);

    /**
     * 根据内容自动判断类型
     */
    void evalType(String tab, int codeIndent, int listIndent) {
        //
        // 处理链接引用行
        // ^\s{0,3}\[([^\]]+)\]:\s*(.+)$
        //
        Matcher m = P_LNK_REF.matcher(rawData);
        if (m.find()) {
            this.type = LineType.LINK_REFER;
            this.prefix = Ws.trim(m.group(1));
            this.content = Ws.trim(m.group(2));
            return;
        }

        m = P.matcher(rawData);
        // 空行
        if (!m.find()) {
            this.type = LineType.BLANK;
            return;
        }

        // 得到头部空格数量
        String tailSpace = m.group(23);
        String sh = m.group(1).replace("\t", tab);
        this.space = sh.length();
        this.trimed = m.group(2) + tailSpace;
        this.level = this.space / listIndent;

        // 无序列表
        if (null != m.group(4)) {
            this.type = LineType.LIST;
            this.listType = ListType.UL;
            this.prefix = m.group(5);
            this.content = m.group(6) + tailSpace;
            return;
        }
        // 有序列表
        if (null != m.group(7)) {
            this.type = LineType.LIST;
            this.listType = ListType.OL;
            this.prefix = m.group(8);
            this.startNumber = Integer.parseInt(m.group(9));
            this.content = m.group(10) + tailSpace;
            return;
        }
        // 缩进代码块
        if (this.space >= codeIndent) {
            this.type = LineType.CODE_BLOCK;
            this.codeType = CodeType.INDENT;
            this.space -= codeIndent;
            String sp = Ws.repeat(' ', this.space);
            this.content = sp + this.trimed;
            return;
        }
        // 分隔线
        if (null != m.group(3)) {
            this.type = LineType.HR;
            this.content = m.group(2);
            return;
        }
        // 引用块
        if (null != m.group(11)) {
            this.type = LineType.BLOCKQUOTE;
            this.prefix = m.group(12);
            this.content = m.group(14) + tailSpace;
            return;
        }
        // 代码块
        if (null != m.group(15)) {
            this.type = LineType.CODE_BLOCK;
            this.codeType = CodeType.FENCED;
            this.prefix = m.group(16);
            this.content = Ws.trim(m.group(17));
            return;
        }
        // 标题
        if (null != m.group(18)) {
            this.type = LineType.HEADING;
            this.prefix = m.group(19);
            this.level = Ws.countChar(this.prefix, '#');
            this.content = m.group(20) + tailSpace;
            return;
        }
        // 表格分隔线
        if (null != m.group(21)) {
            this.type = LineType.TABKE_HEAD_LINE;
            this.content = m.group(21);
            return;
        }

        // 默认就算是段落
        this.content = m.group(22) + tailSpace;
        if (Strings.isBlank(this.content)) {
            this.type = LineType.BLANK;
        } else {
            this.type = LineType.PARAGRAPH;
        }
    }

    void unshiftSpace() {
        this.unshiftSpace(this.space);
    }

    void unshiftSpace(int space) {
        int n = Math.min(space, this.space);
        if (n > 0) {
            this.space -= n;
            String s = Ws.repeat(' ', n);
            this.content = s + this.content;
        }
    }

}
