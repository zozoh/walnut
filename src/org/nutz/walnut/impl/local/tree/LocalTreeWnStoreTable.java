package org.nutz.walnut.impl.local.tree;

import java.io.File;

import org.nutz.lang.Each;
import org.nutz.walnut.api.io.WnHistory;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnStoreTable;
import org.nutz.walnut.impl.local.Locals;

public class LocalTreeWnStoreTable implements WnStoreTable {

    String rootPath;

    File home;

    public LocalTreeWnStoreTable(File home, String rootPath) {
        this.home = home;
        this.rootPath = rootPath;
    }

    @Override
    public int eachHistory(WnObj o, long nano, Each<WnHistory> callback) {
        WnHistory his = getHistory(o, nano);
        callback.invoke(0, his, 1);
        return 1;
    }

    @Override
    public WnHistory getHistory(WnObj o, long nano) {
        // 试着根据对象的全路径获取一下本地文件
        File f = Locals.eval_local_file(home, rootPath, o);

        // 文件不存在，返回空
        if (!f.exists())
            return null;

        // 返回历史记录对象
        return new LocalFileWnHistory(o.id(), f);
    }

    @Override
    public void _clean_for_unit_test() {}

    @Override
    public WnHistory addHistory(String oid, String data, String sha1, long len) {
        return null;
    }

    @Override
    public int cleanHistory(WnObj o, long nano) {
        return 0;
    }

    @Override
    public int cleanHistoryBy(WnObj o, int remain) {
        return 0;
    }

}
