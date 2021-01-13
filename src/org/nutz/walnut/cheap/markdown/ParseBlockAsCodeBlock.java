package org.nutz.walnut.cheap.markdown;

import java.util.Iterator;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

public class ParseBlockAsCodeBlock implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        // 创建块对应的标签，同时也会自动加入行号属性以及 .as-md 的类选择器
        CheapElement $el = ing.createElement("pre", block);

        // 加入文档树
        $el.appendTo(ing.$current);

        // 准备遍历
        Iterator<CheapLine> it = block.lines.iterator();
        if (!it.hasNext()) {
            return;
        }

        // 代码类型
        CheapLine first = it.next();
        CodeType codeType = first.codeType;

        // 标记类型
        $el.attr("md-code-type", codeType.toString());

        // 前缀
        if (!Ws.isBlank(first.prefix)) {
            $el.attr("md-code-prefix", first.prefix);
        }

        // 内容类型
        if (!Ws.isBlank(first.content)) {
            $el.attr("md-code-content", first.content);
        }

        // 拼合代码
        StringBuilder sb = new StringBuilder();

        // 首行
        if (it.hasNext()) {
            CheapLine line = it.next();
            sb.append(line.content);
        }

        // 余行
        while (it.hasNext()) {
            CheapLine line = it.next();
            sb.append('\n').append(line.content);
        }

        // 记入节点
        $el.appendText(sb.toString());
    }

}
