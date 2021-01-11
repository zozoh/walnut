package org.nutz.walnut.cheap.markdown;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;
import org.nutz.walnut.util.Ws;

class CheapLine {

    /**
     * 行号（0 base）
     */
    int number;

    /**
     * 制表符宽度, 一个 '\t' 相当于多少个空格
     */
    private int tabWidth;

    /**
     * 行类型
     */
    BlockType type;

    /**
     * 多少个空格
     */
    int space;

    /**
     * 根据空格计算的缩进级别。
     */
    int indent;

    /**
     * 逻辑级别
     * 
     * <ul>
     * <li>标题: 则表示标题的大纲级别(1 base)
     * <li>引用: 则表示引用的缩进级别(1 base)
     * <li>其他统统为 0
     * </ul>
     */
    int level;
    
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
     * 行原始内容
     */
    String rawData;

    /**
     * 行的实际有效内容
     */
    String content;

    /**
     * 截取前置空白，用来自动匹配行类型的
     */
    String trimed;

    CheapLine(int number, int tabWidth, String str) {
        this.tabWidth = tabWidth;
        this.number = number;
        this.rawData = str;
        this.content = str;
    }

    private Pattern P_SPACE = Regex.getPattern("^([ \t]*)(.*)$");

    /**
     * 自动计算当前行有多少个逻辑空白，和多少个逻辑缩进。 <br>
     * 同时将行内容缩进的内容裁剪掉。
     */
    void evalSpace() {
        // 防守一道
        if (tabWidth <= 0)
            return;

        // 先把前面搞出来
        Matcher m = P_SPACE.matcher(content);
        if (m.find()) {
            String prefix = m.group(1);
            content = m.group(2);
            trimed = content;

            // 搞一下前缀，前面的 \t 统统换成空格
            String tab = Ws.repeat(' ', tabWidth);
            prefix.replace("\t", tab);

            int sn = prefix.length();
            this.indent = sn / tabWidth;
            this.space = this.indent * tabWidth;

            // 嗯，还有剩余的空格，加回去
            if (this.space < sn) {
                content = prefix.substring(this.space) + content;
            }
        }
    }

    private Pattern P_HR = Regex.getPattern("^-{3,}\\s*$");
    private Pattern P_UL = Regex.getPattern("^([+*-]) (.+)$");
    private Pattern P_OL = Regex.getPattern("^(\\d+)\\. (.+)$");
    private Pattern P_BLOCKQUOTE = Regex.getPattern("^((>\\s*)+)(.+)$");
    private Pattern P_CODE_BLOCK = Regex.getPattern("^(`{3,})(.*)$");

    /**
     * 根据内容自动判断类型
     */
    void evalType() {
        // 分隔线
        if (P_HR.matcher(trimed).find()) {
            this.type = BlockType.HR;
            return;
        }
        // 无序列表
        Matcher m = P_UL.matcher(trimed);
        if(m.find()) {
            this.type = BlockType.UL;
            this.prefix = m.group(1);
            this.content = m.group(2);
            return;
        }
        // 有序列表
        m = P_OL.matcher(trimed);
        if(m.find()) {
            this.type = BlockType.OL;
            this.prefix = m.group(1);
            this.content = m.group(2);
            return;
        }
        // 引用块
        m = P_BLOCKQUOTE.matcher(trimed);
        if(m.find()) {
            this.type = BlockType.BLOCKQUOTE;
            this.prefix = m.group(1);
            this.level = this.prefix.replaceAll("\\s", "").length();
            this.content = m.group(3);
            return;
        }
        // 代码块
        m = P_CODE_BLOCK.matcher(trimed);
        if(m.find()) {
            this.type = BlockType.CODE_BLOCK;
            this.prefix = m.group(1);
            this.content = m.group(2);
            return;
        }
        
        // 默认就算是段落
        this.type = BlockType.PARAGRAPH;
    }

    /**
     * 修改缩进值，并同时修改行前的空白。
     * <p>
     * 这个函数主要给制表符缩进的代码块预备的
     * 
     * @param indent
     *            目标缩进值。
     */
    void shiftIndentTo(int indent) {
        if (this.indent > indent) {
            int n = this.indent - indent;
            String tab = Ws.repeat(' ', tabWidth * n);
            this.content = tab + this.content;
            this.indent = indent;
            this.space = indent * tabWidth;
        }
    }

}
