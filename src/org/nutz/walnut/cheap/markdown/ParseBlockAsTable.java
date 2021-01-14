package org.nutz.walnut.cheap.markdown;

import java.util.Iterator;
import java.util.List;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

public class ParseBlockAsTable implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        Iterator<CheapLine> it = block.lines.iterator();

        // 创建块对应的标签，同时也会自动加入行号属性以及 .as-md 的类选择器
        CheapElement $table, $thead, $tbody, $tr, $th, $td;
        $table = ing.createElement("table", block).appendTo(ing.$current);
        $thead = ing.createElement("thead", block).appendTo($table);
        $tbody = ing.createElement("tbody", block).appendTo($table);

        //
        // 处理表头
        //
        CheapLine line = it.next();
        $tr = ing.createElement("tr", block).appendTo($thead);

        List<String> cells = Ws.splitQuote(line.content, "`", "|");
        for (String cell : cells) {
            $th = ing.createElement("th", line).appendTo($tr);
            ing.$current = $th;
            ing.parseLine(cell);
        }

        // 处理表格分隔线
        // ... 好像没啥需要处理的 ^_^!
        it.next();

        //
        // 处理表体
        //
        while (it.hasNext()) {
            line = it.next();
            $tr = ing.createElement("tr", block).appendTo($tbody);

            cells = Ws.splitQuote(line.content, "`", "|");
            for (String cell : cells) {
                $td = ing.createElement("td", line).appendTo($tr);
                ing.$current = $td;
                ing.parseLine(cell);
            }
        }

        // 恢复到当前标签
        ing.$current = $table.parentElement();

    }

}
