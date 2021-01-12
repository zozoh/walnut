package org.nutz.walnut.cheap.markdown;

import java.util.LinkedList;

/**
 * 解析文档时用到的临时块
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
class CheapBlock {

    LineType type;

    ListType listType;

    CodeType codeType;

    LinkedList<CheapLine> lines;

    CheapBlock() {
        lines = new LinkedList<>();
    }

    CheapBlock(CheapLine line) {
        lines = new LinkedList<>();
        lines.add(line);
        type = line.type;
        listType = line.listType;
        codeType = line.codeType;
    }

    CheapLine line(int index) {
        return lines.get(index);
    }

    void appendLine(CheapLine line) {
        lines.add(line);
        // 空行不能确定这个块的类型
        if (LineType.BLANK == this.type) {
            this.type = line.type;
        }
    }

    CheapLine lastLine() {
        return lines.getLast();
    }
}
