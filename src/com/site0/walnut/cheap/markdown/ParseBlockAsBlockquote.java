package com.site0.walnut.cheap.markdown;

import java.util.LinkedList;
import com.site0.walnut.cheap.dom.CheapElement;

public class ParseBlockAsBlockquote implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        // 创建块对应的标签，同时也会自动加入行号属性以及 .as-md 的类选择器
        CheapElement $el = ing.createElement("BLOCKQUOTE", block);

        // 加入文档树
        ing.$current = $el.appendTo(ing.$current);

        // 逐行扫描，形成一个新的块并解析
        String[] list = new String[block.lines.size()];
        int firstLineNumber = block.line(0).lineNumber;
        int i = 0;
        for (CheapLine line : block.lines) {
            list[i++] = line.content;
        }

        // 准备块扫描器
        CheapBlockParsing _PB = ing.BP.clone();

        // 重新解析块
        LinkedList<CheapBlock> subs = _PB.invoke(list, false);

        // 逐块解析
        for (CheapBlock sub : subs) {
            // 重置行号
            for (CheapLine line : sub.lines) {
                line.lineNumber += firstLineNumber;
            }
            // 解析块
            ParseBlock parser = ing.checkParser(sub.type);
            parser.invoke(ing, sub);
        }

        // 恢复到当前标签
        ing.$current = $el.parentElement();
    }

}
