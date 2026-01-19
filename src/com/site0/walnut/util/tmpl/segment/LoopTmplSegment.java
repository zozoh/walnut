package com.site0.walnut.util.tmpl.segment;

import org.nutz.mapl.Mapl;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.each.WnBreakException;
import com.site0.walnut.util.each.WnContinueException;
import com.site0.walnut.util.each.WnEachIteratee;
import com.site0.walnut.util.tmpl.WnTmplRenderContext;
import com.site0.walnut.util.tmpl.util.BreakSegmentException;
import com.site0.walnut.util.tmpl.util.ContinueSegmentException;

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

    /**
     * 下标变量，开始的数值，默认 0
     */
    private int base;

    public LoopTmplSegment() {
        this.base = 0;
    }

    @Override
    public void renderTo(WnTmplRenderContext rc) {
        if (null == children) {
            return;
        }
        Object oldVar = rc.context.get(varName);
        Object oldInx = null;
        if (null != indexName) {
            oldInx = rc.context.get(indexName);
        }
        // 得到循环对象
        Object obj = Mapl.cell(rc.context, looperName);

        // 迭代逻辑
        final int baseI = this.base;
        WnEachIteratee<Object> iteratee = new WnEachIteratee<Object>() {
            public void invoke(int index, Object ele, Object src)
                    throws WnContinueException, WnBreakException {
                rc.context.put(varName, ele);
                if (null != indexName) {
                    rc.context.put(indexName, index + baseI);
                }
                try {
                    for (TmplSegment seg : children) {
                        seg.renderTo(rc);
                    }
                }
                catch (ContinueSegmentException e) {
                    throw new WnContinueException();
                }
                catch (BreakSegmentException e) {
                    throw new WnBreakException();
                }
            }
        };
        // 循环对象
        try {
            Wlang.each(obj, iteratee);
        }
        // 恢复
        finally {
            rc.context.put(varName, oldVar);
            if (null != indexName) {
                rc.context.put(indexName, oldInx);
            }
        }
    }

    /**
     * 解析一个字符串,格式类似下面的字符串
     * 
     * <pre>
     * {var},{index}=${base} :{looper}
     *   it ,index=2   : alist
     * </pre>
     * 
     * @param input
     * 
     * @return 自身
     */
    public LoopTmplSegment valueOf(String input) {
        String[] ss = Ws.splitIgnoreBlank(input, ":");
        // 只有 looper alist
        if (ss.length == 1) {
            this.looperName = ss[0];
        }
        // it,index : alist
        else if (ss.length > 1) {
            this.looperName = ss[1];
            String[] vv = Ws.splitIgnoreBlank(ss[0]);
            this.varName = vv[0];
            // it,index=1 : alist
            if (vv.length > 1) {
                String ixName = vv[1];
                int pos = ixName.indexOf('=');
                if (pos > 0) {
                    this.indexName = ixName.substring(0, pos);
                    this.base = Integer
                        .parseInt(ixName.substring(pos + 1).trim());
                } else {
                    this.indexName = ixName;
                }

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

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

}
