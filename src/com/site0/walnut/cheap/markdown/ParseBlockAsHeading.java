package com.site0.walnut.cheap.markdown;

import com.site0.walnut.cheap.dom.CheapElement;

public class ParseBlockAsHeading implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        // 创建块对应的标签，同时也会自动加入行号属性以及 .as-md 的类选择器
        String tagName = "H" + block.line(0).level;
        CheapElement $el = ing.createElement(tagName, block);

        // 加入文档树 & 压入当前解析段落标签
        $el.appendTo(ing.$current);
        ing.$current = $el;

        // 逐行解析
        for (CheapLine line : block.lines) {
            ing.parseLine(line);
        }

        // 恢复到当前标签
        ing.$current = $el.parentElement();
    }

}
