package org.nutz.walnut.cheap.markdown;

import java.util.LinkedList;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

/**
 * 解析文档的运行时对象。包括解析过程需要用到的栈，以及中间结果
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class CheapMarkdownParsing {

    /**
     * 制表符宽度
     */
    private int tabWidth;

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
     * 正在扫描的块
     */
    private LinkedList<CheapBlock> stack;

    public CheapMarkdownParsing(int tabWidth) {
        this.header = new NutMap();
        this.blocks = new LinkedList<>();
        this.stack = new LinkedList<>();
        this.tabWidth = tabWidth;
        this.$body = new CheapElement("body");
        this.$current = null;
    }

    public CheapDocument parseDoc(String[] lines) {
        // 扫描文档头
        int offset = this.scanHeader(lines, 0);

        // 扫描文档体
        offset = this.scanBody(lines, offset);

        // 根据扫描出来的文档块，深入解析文档结构

        return null;
    }

    private int scanBody(String[] lines, int offset) {
        return offset;
    }

    private int scanHeader(String[] lines, int offset) {
        if (offset >= lines.length) {
            return offset;
        }

        // 判断起始标记: ---
        String str = lines[offset];
        CheapLine line = new CheapLine(offset, tabWidth, str);
        line.evalSpace();
        line.evalType();

        // 未发现
        if (BlockType.HR != line.type) {
            return offset;
        }

        // 逐行扫描，知道遇到 --- 标记
        String lastName = null;
        for (; offset < lines.length; offset++) {
            str = lines[offset];
            line = new CheapLine(offset, tabWidth, str);
            line.evalSpace();
            line.evalType();

            // 退出
            if (BlockType.HR == line.type) {
                break;
            }

            // 标签
            if (BlockType.UL == line.type) {
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
