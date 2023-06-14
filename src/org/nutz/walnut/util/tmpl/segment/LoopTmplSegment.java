package org.nutz.walnut.util.tmpl.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.each.WnEachIteratee;

public class LoopTmplSegment extends AbstractTmplSegment {

    /**
     * 循环变量名称
     */
    private String varName;

    /**
     * 下标变量名，空的话就没有下标变量
     */
    private String indexName;

    /**
     * 从上下文这个变量循环
     */
    private String looperName;

    @Override
    public void renderTo(NutBean context, boolean showKey, StringBuilder sb) {
        if (null == children) {
            return;
        }
        Object oldVar = context.get(varName);
        Object oldInx = null;
        if (null != indexName) {
            oldInx = context.get(indexName);
        }
        // 得到循环对象
        Object obj = Mapl.cell(context, looperName);

        // 迭代逻辑
        WnEachIteratee<Object> iteratee = new WnEachIteratee<Object>() {
            public void invoke(int index, Object ele, Object src) {
                context.put(varName, ele);
                if (null != indexName) {
                    context.put(indexName, index);
                }

                for (TmplSegment seg : children) {
                    seg.renderTo(context, showKey, sb);
                }
            }
        };
        // 循环对象
        try {
            Wlang.each(obj, iteratee);
        }
        // 恢复
        finally {
            context.put(varName, oldVar);
            if (null != indexName) {
                context.put(indexName, oldInx);
            }
        }
    }

    /**
     * 解析一个字符串,格式类似下面的字符串
     * 
     * <pre>
     * {var},{index} :{looper}
     *   it ,index   : alist
     * </pre>
     * 
     * @param input
     * 
     * @return 自身
     */
    public LoopTmplSegment valueOf(String input) {
        String[] ss = Ws.splitIgnoreBlank(input, ":");
        if (ss.length == 1) {
            this.looperName = ss[0];
        } else if (ss.length > 1) {
            this.looperName = ss[1];
            String[] vv = Ws.splitIgnoreBlank(ss[0]);
            this.varName = vv[0];
            if (vv.length > 1) {
                this.indexName = vv[1];
            }
        }
        return this;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getLooperName() {
        return looperName;
    }

    public void setLooperName(String looperName) {
        this.looperName = looperName;
    }

}
