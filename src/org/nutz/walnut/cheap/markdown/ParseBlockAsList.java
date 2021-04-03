package org.nutz.walnut.cheap.markdown;

import java.util.LinkedList;
import java.util.ListIterator;

import org.nutz.lang.Lang;
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

        // 存储嵌入块行
        int firstLineNumber = -1;
        LinkedList<String> subLines = null;

        // 逐行解析
        while (it.hasNext()) {
            CheapLine line = it.next();

            // 嵌入缩进块，扫描收集到一个块了，重新进行分析
            if (CodeType.INDENT == line.codeType) {
                int unshift = currentListLevel * ing.BP.listIndent;
                line.unshiftSpace(unshift);
                if (null == subLines) {
                    firstLineNumber = line.lineNumber;
                    subLines = new LinkedList<>();
                }
                subLines.add(line.content);
                continue;
            }

            // 如果之前有了嵌入块行，那么先处理离线
            if (null != subLines) {
                // 准备解析器分身
                CheapMarkdownParsing mdp = ing.clone();
                mdp.autoBr = false;
                mdp.unwrapParagraph = true;

                // 扫描块
                String[] subs = new String[subLines.size()];
                subLines.toArray(subs);
                LinkedList<CheapBlock> subBlocks = mdp.BP.invoke(subs, false);

                // 循环处理块，嵌入到当前列表项
                for (CheapBlock sub : subBlocks) {
                    // 重置行号
                    for (CheapLine subLine : sub.lines) {
                        subLine.lineNumber += firstLineNumber;
                    }
                    // 插入前置断行文本
                    ing.doc.createPlaceholdText("\n").appendTo(ing.$current);
                    // 解析块
                    ParseBlock parser = mdp.checkParser(sub.type);
                    parser.invoke(mdp, sub);
                }

                // 清空
                subLines = null;
                firstLineNumber = -1;
            }

            // 如果是空行
            if (LineType.BLANK == line.type) {
                CheapElement $p = ing.createElement("p", "as-blank", line);
                $p.appendTo(ing.$current);
            }
            // 如果是一个 LI
            else if (LineType.LIST == line.type) {
                // 同级列表
                if (currentListLevel == line.level) {
                    $li = ing.createElement("li", line);
                    ing.$current = $li.appendTo($list);
                    ing.parseLine(line);
                }
                // 上一级列表
                else if (currentListLevel > line.level) {
                    // 寻找第一个基本与自己级别相当的祖先
                    String tagName = line.listType.toString();
                    CheapElement $an = ing.$current.getClosest($p -> {
                        if (!$p.isStdTagName(tagName))
                            return false;
                        int level = $p.attrInt("md-level");
                        return level <= line.level;
                    });
                    if (null != $an) {
                        $list = $an;
                    }

                    // 插入列表项
                    $li = ing.createElement("li", line);
                    ing.$current = $li.appendTo($list);
                    ing.parseLine(line);
                    // 修改当前列表级别
                    currentListLevel = $list.attrInt("md-level");
                }
                // 下一级列表
                else {
                    String tagName = line.listType.toString();
                    $list = ing.createElement(tagName, line);
                    $list.appendTo(ing.$current);
                    $list.attr("md-level", line.level);
                    $li = ing.createElement("li", line);
                    ing.$current = $li.appendTo($list);
                    ing.parseLine(line);
                    // 修改当前列表级别
                    currentListLevel = line.level;
                }
            }
            // 其他是不可能的
            else {
                throw Lang.impossible();
            }
        }

        // 恢复到当前标签
        ing.$current = $oldCurrent;

    }

}
