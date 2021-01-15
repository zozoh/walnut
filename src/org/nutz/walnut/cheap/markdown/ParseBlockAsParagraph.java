package org.nutz.walnut.cheap.markdown;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapNode;

public class ParseBlockAsParagraph implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        CheapNode $oldCurrent = ing.$current;
        CheapElement $p = (CheapElement) $oldCurrent;

        // 创建块对应的标签，同时也会自动加入行号属性以及 .as-md 的类选择器
        if (!ing.unwrapParagraph) {
            $p = ing.createElement("P", block);
            // 加入文档树 & 压入当前解析段落标签
            $p.appendTo(ing.$current);
            ing.$current = $p;
        }

        // 逐行解析
        CheapElement $br = null;
        for (CheapLine line : block.lines) {
            ing.parseLine(line);
            // 插入换行符: 当前段落下的直接文本，可能需要插入这个换行符
            // 当然，如果在自定义的 HTML标签里，就不必了
            // 如果当前行最后一个元素是块元素（通常也是自定义 HTML搞的）
            // 也不必插入回行。 当然这里的“最后一个”必须是逻辑上的非空元素
            CheapNode $last = ing.$current.getLastNoBlankChild();
            if (ing.autoBr
                && ing.$current == $p
                && null != $last
                && (!$last.isElement() // 不是块元素
                    || !ing.doc.isHtmlBlockTag((CheapElement) $last))) {
                $br = ing.createElement("br", line);
                $br.setPlacehold(true);
                $br.appendTo(ing.$current);
            }
            // 否则就插入一个换行文本好了
            else {
                ing.doc.createPlaceholdText("\n").appendTo(ing.$current);
                $br = null;
            }
        }

        // 最后一个 BR 删掉
        if (null != $br) {
            $br.remove();
        }

        // 恢复到当前标签
        ing.$current = $oldCurrent;
    }

}
