package org.nutz.walnut.cheap.markdown;

import org.nutz.walnut.cheap.dom.CheapElement;

public class ParseBlockAsLinkRefer implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        // 创建块对应的标签，同时也会自动加入行号属性以及 .as-md 的类选择器
        CheapElement $el = ing.createElement("dl", block);

        // 逐行解析
        for (CheapLine line : block.lines) {
            CheapElement $dfn = ing.createElement("dfn", line);
            $dfn.attr("name", line.prefix);
            $dfn.appendText(line.content);
            $dfn.appendTo($el);
        }

        // 加入文档树
        $el.appendTo(ing.$current);
    }

}
