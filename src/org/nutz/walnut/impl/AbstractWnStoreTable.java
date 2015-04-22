package org.nutz.walnut.impl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStoreTable;

public abstract class AbstractWnStoreTable implements WnStoreTable {

    @Override
    public List<WnHistory> getHistoryList(WnObj o, long nano) {
        final List<WnHistory> list = new LinkedList<WnHistory>();
        eachHistory(o, nano, new Each<WnHistory>() {
            public void invoke(int index, WnHistory his, int length) {
                list.add(his);
            }
        });
        return list;
    }

    @Override
    public WnHistory getHistory(WnObj o, long nano) {
        final WnHistory[] his = new WnHistory[1];
        eachHistory(o, nano, new Each<WnHistory>() {
            public void invoke(int index, WnHistory ele, int length) {
                his[0] = ele;
                Lang.Break();
            }
        });
        return his[0];
    }

}
