package org.nutz.walnut.cheap.markdown;

import java.util.List;
import java.util.ListIterator;

import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapNode;

public class ParseBlockAsList implements ParseBlock {

    @Override
    public void invoke(CheapMarkdownParsing ing, CheapBlock block) {
        // 根据第一行确定类型
        ListIterator<CheapLine> it = block.lines.listIterator();
        CheapLine first = it.next();
        int currentListLevel = first.level;
        CheapNode $oldCurrent = ing.$current;

        // 创建块对应的标签，同时也会自动加入行号属性以及 .as-md 的类选择器
        CheapElement $list = ing.createElement(first.listType.toString(), block);

        // 加入文档树 & 压入当前解析段落标签
        $list.appendTo(ing.$current);
        $list.attr("md-level", currentListLevel);

        // 第一行的 li
        CheapElement $li = ing.createElement("li", first);
        ing.$current = $li.appendTo($list);
        ing.parseLine(first);

        // 逐行解析
        while (it.hasNext()) {
            CheapLine line = it.next();

            // 如果是段落
            if (LineType.PARAGRAPH == line.type) {
                CheapElement $p = ing.createElement("div", "in-list", line);
                ing.$current = $p;
                ing.parseLine(line);
                ing.$current = $p.parentElement();
            }
            // 如果是一个 LI
            else if (LineType.LIST == line.type) {
                // 同级列表
                if (currentListLevel == line.level) {
                    $li = ing.createElement("li", first);
                    ing.$current = $li.appendTo($list);
                    ing.parseLine(first);
                }
                // 上一级列表
                else if (currentListLevel > line.level) {
                    // 向上查找到属于自己的列表元素
                    String tagName = line.listType.toString();
                    List<CheapElement> $plist = ing.$current.getAncestors(tagName);

                    // 寻找第一个基本与自己相当的祖先
                    for (CheapElement $p : $plist) {
                        int level = $p.attrInt("md-level");
                        if (level <= line.level) {
                            currentListLevel = line.level;
                            $list = $p;
                            break;
                        }
                    }

                    // 插入列表项
                    $li = ing.createElement("li", first);
                    ing.$current = $li.appendTo($list);
                    ing.parseLine(line);
                }
                // 下一级列表
                else {
                    $list = ing.createElement(line.listType.toString(), line)
                               .appendTo(ing.$current);
                    $list.attr("md-level", line.level);
                    $li = ing.createElement("li", first);
                    ing.$current = $li.appendTo($list);
                    ing.parseLine(line);
                }
            }
            // 其他的项目，递归解析
            else {
                ParseBlock parser = ing.checkParser(line.type);
                // 寻找并构造一个子块
                CheapBlock sub = new CheapBlock(line);
                while (it.hasNext()) {
                    CheapLine next = it.next();
                    // 回退，并退出
                    if (sub.type != next.type) {
                        it.previous();
                        break;
                    }
                    // 追加入当前块
                    else {
                        sub.appendLine(next);
                    }
                }
                // 解析这个子块
                parser.invoke(ing, sub);
            }
        }

        // 恢复到当前标签
        ing.$current = $oldCurrent;

    }

}
