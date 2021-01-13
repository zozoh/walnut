package org.nutz.walnut.cheap.markdown;

import org.nutz.walnut.cheap.dom.CheapElement;

public class ParseBlockAsHr implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        CheapElement $el = ing.createElement("hr", block);
        $el.attr("md-hr-size", block.line(0).content.length());
        $el.appendTo(ing.$current);
    }

}
