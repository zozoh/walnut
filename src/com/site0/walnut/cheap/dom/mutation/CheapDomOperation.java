package com.site0.walnut.cheap.dom.mutation;

import java.util.List;

import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.selector.CheapDomSelector;

public class CheapDomOperation {

    private CheapDomSelector[] selector;

    private CheapDomMutation mutation;

    private CheapDomOperation[] children;

    public void operate(CheapElement el) {
        // 逐个选择器选择节点
        for (CheapDomSelector sel : selector) {
            // 选择节点
            List<CheapElement> list = el.selectAll(sel);

            // 木有节点，下一个
            if (null == list || list.isEmpty()) {
                continue;
            }

            // 执行节点改变操作
            if (null != mutation) {
                for (CheapElement li : list) {
                    List<CheapElement> subs = mutation.mutate(li);
                    // 执行子操作
                    if (null != children && null != subs && !subs.isEmpty()) {
                        for (CheapDomOperation op : children) {
                            for (CheapElement sub : subs) {
                                op.operate(sub);
                            }
                        }
                    }
                }
            }
        }
    }

    public CheapDomSelector[] getSelector() {
        return selector;
    }

    public void setSelector(CheapDomSelector[] selector) {
        this.selector = selector;
    }

    public CheapDomMutation getMutation() {
        return mutation;
    }

    public void setMutation(CheapDomMutation mutation) {
        this.mutation = mutation;
    }

    public CheapDomOperation[] getChildren() {
        return children;
    }

    public void setChildren(CheapDomOperation[] items) {
        this.children = items;
    }

}
