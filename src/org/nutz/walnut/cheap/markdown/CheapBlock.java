package org.nutz.walnut.cheap.markdown;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 解析文档时用到的临时块
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
class CheapBlock {

    /**
     * 本块缩进，4 个空格或者 1个 tab 算一个缩进
     */
    int indent;

    BlockType type;

    CheapBlock parent;

    LinkedList<CheapBlock> children;

    LinkedList<CheapLine> lines;

    public CheapBlock() {
        lines = new LinkedList<>();
    }

    boolean hasChildren() {
        return null != children && !children.isEmpty();
    }

    void append(CheapBlock... blocks) {
        this.add(-1, blocks);
    }

    void prepend(CheapBlock... blocks) {
        this.add(0, blocks);
    }

    void add(int index, CheapBlock... blocks) {
        if (null == children) {
            children = new LinkedList<>();
        }
        List<CheapBlock> list = new ArrayList<>(blocks.length);
        // 最后一个
        if (index == -1) {
            this.children.addAll(list);
        }
        // 在特殊位置插入
        else {
            // 倒数
            if (index < 0) {
                index = Math.max(0, children.size() + index);
            }
            this.children.addAll(index, list);
        }
    }

}
