package org.nutz.walnut.cheap.markdown;

import java.util.Iterator;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

public class ParseBlockAsCodeBlock implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        // 创建块对应的标签，同时也会自动加入行号属性以及 .as-md 的类选择器
        CheapElement $el = ing.createElement("pre", block);

        // 代码类型
        $el.attr("md-code-type", block.codeType.toString());

        // 加入文档树
        $el.appendTo(ing.$current);

        // 准备遍历
        Iterator<CheapLine> it = block.lines.iterator();
        if (!it.hasNext()) {
            return;
        }

        //
        // 对于围栏代码，首行要读出来，以便知道前缀和内容类型
        //
        if (CodeType.FENCED == block.codeType) {
            // 代码类型
            CheapLine first = it.next();

            // 内容类型
            if (!Ws.isBlank(first.content)) {
                $el.attr("md-code-content", first.content);
            }

            // 前缀
            if (!Ws.isBlank(first.prefix)) {
                $el.attr("md-code-prefix", first.prefix);
            }

        }
          // 默认就是缩进代码咯
        else {

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
