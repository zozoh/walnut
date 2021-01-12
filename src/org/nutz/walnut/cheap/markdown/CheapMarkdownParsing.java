package org.nutz.walnut.cheap.markdown;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

import com.sun.tools.doclint.HtmlTag.BlockType;

/**
 * 解析文档的运行时对象。包括解析过程需要用到的栈，以及中间结果
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class CheapMarkdownParsing {

    /**
     * 制表符宽度
     */
    private String tab;

    private int tabWidth;

    /**
     * 列表一个缩进级别消耗的空格（默认 3）
     */
    private int listIndent;

    /**
     * 一个代码块需要多少空格作为缩进标识（默认 4）
     */
    private int codeIndent;

    /**
     * 文档头
     */
    private NutMap header;

    /**
     * 文档体元素
     */
    private CheapElement $body;

    /**
     * 当前解析元素
     */
    private CheapElement $current;

    /**
     * 扫描完毕的块
     */
    private LinkedList<CheapBlock> blocks;

    /**
     * 当前处理的块
     */
    private CheapBlock block;

    private String[] lines;

    private int offset;

    public CheapMarkdownParsing() {
        this(4);
    }

    public CheapMarkdownParsing(int tabWidth) {
        this.header = new NutMap();
        this.blocks = new LinkedList<>();
        this.listIndent = 3;
        this.codeIndent = 4;
        this.$body = new CheapElement("body");
        this.$current = null;
        this.tab = Ws.repeat(' ', tabWidth);
        this.tabWidth = tabWidth;
    }

    public CheapDocument parseDoc(String[] lines) {
        // 扫描文档体，将行集合成块
        this.parseBlocks(lines);

        // 根据扫描出来的文档块，深入解析文档结构

        return null;
    }

    List<CheapBlock> parseBlocks(String[] lines) {
        this.lines = lines;
        this.offset = 0;

        // 扫描文档头
        this.offset = this.scanHeader();

        // 扫描文档体，将行集合成块
        this.offset = this.scanBlocks();

        // 得到扫描的块列表
        return blocks;
    }

    private String getCodeLine(String line) {
        int len = line.length();
        int space = 0;
        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);
            if (' ' == c) {
                space++;
            } else if ('\t' == c) {
                space += tabWidth;
            }
            // 满足条件
            if (space >= codeIndent) {
                return line.substring(i + 1);
            }
        }
        return null;
    }

    private void pushBlock() {
        if (null != block) {
            // 最后一个空行木有必要了
            if (LineType.BLANK == block.lastLine().type) {
                block.lines.removeLast();
            }
            blocks.add(block);
            block = null;
        }
    }

    private int scanBlocks() {
        // 开始扫描代码块
        for (; offset < lines.length; offset++) {
            String str = lines[offset];
            CheapLine line = new CheapLine(offset, str);

            // 代码块模式，提前扫描
            if (null != block && LineType.CODE_BLOCK == block.type) {
                // 对于缩进代码块，一直读啊读
                if (CodeType.INDENT == block.codeType) {
                    line = scanMarkdownCodeBlock(line);
                }
                // 对于 GFM 代码块，一直读啊读
                else if (CodeType.GFM == block.codeType) {
                    line = scanGFMCodeBlock(line);
                }
            }

            // 没有行，可能被代码块扫描消耗光了
            if (null == line) {
                continue;
            }

            // 判断行类型
            line.evalType(tab, codeIndent);

            // 判断逻辑缩进
            int listLV = line.space / listIndent;

            // 创建块
            if (null == block) {
                block = new CheapBlock(line);
            }
            // 段落行是没有个性的，见谁加谁，除非前面一行是空行
            else if (LineType.PARAGRAPH == line.type) {
                // 如果最后一行是空行，且当前行没有缩进
                // 那么也一定是加不进去的
                if (LineType.BLANK == block.lastLine().type && listLV == 0) {
                    this.pushBlock();
                    block = new CheapBlock(line);
                }
                // 否则就要加入当前块，可能是代码块，表格，列表，引用等等
                else {
                    block.appendLine(line);
                }
            }
            // 遇到不同的块类型
            else if (block.type != line.type) {
                this.pushBlock();
                if (LineType.BLANK != line.type) {
                    block = new CheapBlock(line);
                }
            }
            // 默认就是加入咯
            else {
                block.appendLine(line);
            }

        }
        // 最后一块
        this.pushBlock();

        return offset;
    }

    private CheapLine scanGFMCodeBlock(CheapLine line) {
        while (true) {
            line.type = LineType.CODE_BLOCK;
            line.codeType = CodeType.GFM;
            line.content = line.rawData;
            block.appendLine(line);

            // 继续读取
            offset++;
            if (offset >= lines.length) {
                line = null;
                break;
            }
            String s2 = lines[offset];
            // 块结束
            if ("```".equals(s2.trim())) {
                this.pushBlock();
                break;
            }
            line = new CheapLine(offset, s2);
        }
        return null;
    }

    private CheapLine scanMarkdownCodeBlock(CheapLine line) {
        while (true) {
            // 空行一定加入
            if (Ws.isBlank(line.rawData)) {
                line.type = LineType.CODE_BLOCK;
                line.codeType = CodeType.INDENT;
                line.content = line.rawData;
                block.appendLine(line);
            }
            // 缩进也加入
            else {
                String s2 = this.getCodeLine(line.rawData);
                if (null != s2) {
                    line.type = LineType.CODE_BLOCK;
                    line.codeType = CodeType.INDENT;
                    line.content = s2;
                    block.appendLine(line);
                }
                // 其他的行，不能接受，退出
                else {
                    this.pushBlock();
                    break;
                }
            }
            // 继续读取
            offset++;
            if (offset >= lines.length) {
                line = null;
                break;
            }
            String str = lines[offset];
            line = new CheapLine(offset, str);
        }
        return line;
    }

    private int scanHeader() {
        // 判断起始标记: ---
        String str = lines[offset];
        CheapLine line = new CheapLine(offset, str);
        line.evalType(tab, codeIndent);

        // 未发现
        if (LineType.HR != line.type) {
            return offset;
        }

        // 逐行扫描，知道遇到 --- 标记
        String lastName = null;
        for (; offset < lines.length; offset++) {
            str = lines[offset];
            line = new CheapLine(offset, str);
            line.evalType(tab, codeIndent);

            // 退出
            if (LineType.HR == line.type) {
                break;
            }

            // 标签
            if (ListType.UL == line.listType) {
                if (null != lastName) {
                    this.header.addv2(lastName, Ws.trim(line.content));
                }
                continue;
            }

            // 看看行咯
            String[] ss = Ws.splitIgnoreBlank(line.content, ":");
            lastName = ss[0];
            if (ss.length > 1) {
                this.header.put(lastName, ss[1]);
            }
            // 有可能是列表，持续读下去
            else {
                this.header.put(lastName, new LinkedList<String>());
            }

        }

        return offset + 1;
    }

}
