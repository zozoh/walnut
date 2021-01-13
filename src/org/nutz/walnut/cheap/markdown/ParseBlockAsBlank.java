package org.nutz.walnut.cheap.markdown;

import org.nutz.walnut.cheap.dom.CheapElement;

public class ParseBlockAsBlank implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        CheapElement $el = ing.createElement("p", block);

        // 逐行解析
        for (CheapLine line : block.lines) {
            // 插入换行符
            if (ing.autoBr) {
                ing.createElement("br", line).append(ing.$current);
            }
        }

        // 最后一个 BR 删掉
        if (ing.autoBr) {
            ing.$current.removeLastChild();
        }

        // 加入文档树
        $el.appendTo(ing.$current);
    }

}
