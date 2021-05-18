package org.nutz.walnut.ext.data.entity;

import java.util.LinkedList;
import java.util.List;

import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.lang.Strings;
import org.nutz.lang.util.LongRegion;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import org.nutz.walnut.util.Wregion;

public class DaoEntityQuery {

    // ------------------------------------------
    // 排序
    // ------------------------------------------
    private List<DaoEntitySort> sorts;

    // ------------------------------------------
    // Getter / Setter
    // ------------------------------------------

    public List<DaoEntitySort> getSorts() {
        return sorts;
    }

    public void setSorts(List<DaoEntitySort> sorts) {
        this.sorts = sorts;
    }

    public void setSorts(NutMap sort) {
        if (!sort.isEmpty()) {
            for (String key : sort.keySet()) {
                int val = sort.getInt(key);
                this.addSort(key, val == 1);
            }
        }
    }

    public void addSort(DaoEntitySort sort) {
        if (null == this.sorts) {
            this.sorts = new LinkedList<>();
        }
        this.sorts.add(sort);
    }

    public void addSort(String name, boolean asc) {
        this.addSort(new DaoEntitySort(name, asc));
    }

    // ------------------------------------------
    // 帮助函数
    // ------------------------------------------

    protected void joinLike(SqlExpressionGroup we, String name, String val) {
        if (!Strings.isBlank(val)) {
            we.andLike(name, val);
        }
    }

    protected void joinRegion(SqlExpressionGroup we, String name, String str) {
        if (!Strings.isBlank(str)) {
            String s = Wregion.extend_rg_macro(str);
            LongRegion rg = Region.Long(s);
            // Region
            if (rg.isRegion()) {
                // [xxx, )
                if (null == rg.right()) {
                    if (rg.isLeftOpen()) {
                        we.andGT(name, rg.left());
                    } else {
                        we.andGTE(name, rg.left());
                    }
                }
                // (, xxx)
                else if (null == rg.left()) {
                    if (rg.isRightOpen()) {
                        we.andLT(name, rg.right());
                    } else {
                        we.andLTE(name, rg.right());
                    }
                }
                // [xxx, xxx]
                else {
                    we.andBetween(name, rg.left(), rg.right());
                }
            }
        }
    }

    protected void joinStr(SqlExpressionGroup we, String name, String val) {
        if (!Strings.isBlank(val)) {
            we.andEquals(name, val);
        }
    }

    protected void joinBool(SqlExpressionGroup we, String name, Boolean val) {
        if (null != val) {
            we.andEquals(name, val);
        }
    }

    protected void joinSorts(SimpleCriteria cri) {
        if (null != sorts) {
            for (DaoEntitySort sr : sorts) {
                if (sr.isAsc()) {
                    cri.asc(sr.getName());
                } else {
                    cri.desc(sr.getName());
                }
            }
        }
    }

}