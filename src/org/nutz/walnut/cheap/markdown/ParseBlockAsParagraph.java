package org.nutz.walnut.cheap.markdown;

import org.nutz.walnut.cheap.dom.CheapElement;

public class ParseBlockAsParagraph implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        // 创建块对应的标签，同时也会自动加入行号属性以及 .as-md 的类选择器
        CheapElement $el = ing.createElement("P", block);

        // 加入文档树 & 压入当前解析段落标签
        $el.appendTo(ing.$current);
        ing.$current = $el;

        // 逐行解析
        for (CheapLine line : block.lines) {
            ing.parseLine(line);
            // 插入换行符
            if (ing.autoBr) {
                ing.createElement("br", line).append(ing.$current);
            }
        }

        // 恢复到当前标签
        ing.$current = $el.parentElement();

        // 最后一个 BR 删掉
        if (ing.autoBr) {
            ing.$current.removeLastChild();
        }
    }

}
