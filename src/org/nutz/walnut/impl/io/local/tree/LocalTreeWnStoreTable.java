package org.nutz.walnut.impl.io.local.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.AbstractWnStoreTable;
import org.nutz.walnut.impl.io.local.Locals;
import org.nutz.walnut.impl.io.local.data.LocalObjWnHistory;

public class LocalTreeWnStoreTable extends AbstractWnStoreTable {

    String rootPath;

    File home;

    public LocalTreeWnStoreTable(File home, String rootPath) {
        this.home = home;
        this.rootPath = rootPath;
    }

    @Override
    public int eachHistory(WnObj o, long nano, Each<WnHistory> callback) {
        WnHistory his = getHistory(o, nano);
        if (null != his) {
            callback.invoke(0, his, 1);
            return 1;
        }
        return 0;
    }

    @Override
    public WnHistory getHistory(WnObj o, long nano) {
        // 试着根据对象的全路径获取一下本地文件
        File f = Locals.eval_local_file(home, rootPath, o);

        if (nano < 0 || nano >= f.lastModified() * 1000000L) {

            // 文件不存在，返回空
            if (!f.exists())
                return null;

            // 返回历史记录对象
            o.data(f.getAbsolutePath());
            return new LocalObjWnHistory(o);
        }

        return null;
    }

    @Override
    public void _clean_for_unit_test() {}

    @Override
    public WnHistory addHistory(String oid, String data, String sha1, long len) {
        return null;
    }

    @Override
    public List<WnHistory> cleanHistory(WnObj o, long nano) {
        return new ArrayList<WnHistory>(1);
    }

    @Override
    public List<WnHistory> cleanHistoryBy(WnObj o, int remain) {
        return new ArrayList<WnHistory>(1);
    }

}
