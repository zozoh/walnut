package org.nutz.walnut.cheap.dom;

import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;
import org.nutz.walnut.util.Ws;

/**
 * 对于文档格式化的策略
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class CheapFormatter {

    static String NL_TAGS;
    static {
        NL_TAGS = "^(HEAD|TITLE|STYLE"
                  + "|UL|OL|LI"
                  + "|DL|DT|DD"
                  + "|H[1-6]|P|BLOCKQUOTE|PRE|DIV"
                  + "|SCRIPT|TEMPLATE"
                  + "|TABLE|THEAD|TBODY|TFOOT|TR"
                  + "|ARTICLE|ASIDE|ADDRESS|NAV"
                  + "|HEADER|SECTION|FOOTER|MAIN"
                  + "|AUDIO|VIDEO"
                  + "|HR"
                  + "|HEAD|BODY|META|LINKE|TITLE)$";
    }

    public CheapFormatter() {}

    public CheapFormatter(boolean asHTML) {
        if (asHTML) {
            this.setBlockTags(NL_TAGS);
            this.setBreakLineTags("^(BR|DFN|LINK|META)$");
            this.tab = "  ";
        }
    }

    /**
     * 块元素，前后（内外）都要回行
     */
    // private String blockTags;
    private Pattern P_BLOCK_TAGS;

    /**
     * 断行元素，之后后面需要回行
     */
    // private String brTags;
    private Pattern P_BR_TAGS;

    /**
     * 一个缩进用什么展现
     */
    private String tab;

    public boolean isBlock(CheapElement $el) {
        return null != P_BLOCK_TAGS && P_BLOCK_TAGS.matcher($el.tagName).find();
    }

    public boolean isBreakLine(CheapElement $el) {
        return null != P_BR_TAGS && P_BR_TAGS.matcher($el.tagName).find();
    }

    public String getPrefix(int indent) {
        if (indent <= 0)
            return "\n";
        return "\n" + Ws.repeat(tab, indent);
    }

    public String shiftTab(String prefix) {
        return prefix + tab;
    }

    public String unshiftTab(String prefix) {
        if (prefix.endsWith(tab)) {
            return prefix.substring(0, prefix.length() - tab.length());
        }
        return prefix;
    }

    public void setBlockTags(String blockTags) {
        // this.blockTags = blockTags;
        if (null == blockTags) {
            P_BLOCK_TAGS = null;
        } else {
            P_BLOCK_TAGS = Regex.getPattern(blockTags);
        }
    }

    public void setBreakLineTags(String brTags) {
        // this.brTags = brTags;
        if (null == brTags) {
            P_BR_TAGS = null;
        } else {
            P_BR_TAGS = Regex.getPattern(brTags);
        }
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

}
