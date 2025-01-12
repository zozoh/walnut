package com.site0.walnut.core.bm.sql;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.nutz.lang.stream.VoidInputStream;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bm.WnIoReadHandle;

public class SqlBMReadHandle extends WnIoReadHandle {

    private SqlBM bm;

    private InputStream ins;

    public SqlBMReadHandle(SqlBM bm) {
        this.bm = bm;
    }

    // 因为要考虑到滞后设置 obj，所以在第一次读取的时候，才初始化流
    @Override
    protected InputStream input() {
        WnObj o = this.obj;
        if (null != o && null == ins) {
            Blob blob = bm.getBlob(o);
            if (null == blob) {
                return new VoidInputStream();
            }
            try {
                return blob.getBinaryStream();
            }
            catch (SQLException e) {
                throw Er.wrap(e);
            }
        }
        return ins;
    }

}
