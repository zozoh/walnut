package com.site0.walnut.cheap.markdown;

import com.site0.walnut.cheap.dom.CheapElement;

public class ParseBlockAsHr implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        CheapElement $el = ing.createElement("hr", block);
        $el.attr("md-hr-size", block.line(0).content.length());
        $el.appendTo(ing.$current);
    }

}
