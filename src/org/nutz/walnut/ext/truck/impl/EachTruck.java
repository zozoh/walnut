package org.nutz.walnut.ext.truck.impl;

import java.io.IOException;

import org.nutz.lang.Each;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoHandle;
import org.nutz.walnut.core.WnIoHandleMutexException;
import org.nutz.walnut.ext.truck.TruckContext;
import org.nutz.walnut.util.Wn;

abstract class EachTruck implements Each<WnObj> {

    private TruckContext tc;

    EachTruck(TruckContext tc) {
        this.tc = tc;
    }

    void appendObj(WnObj obj) {
        if (null != tc.list) {
            tc.list.add(obj);
        }
    }

    /**
     * 转换索引
     * 
     * @param obj
     *            输入对象
     * 
     * @return newObj
     */
    WnObj transIndexer(WnObj obj) {
        return tc.toIndexer.create(tc.toDir, obj);
    }

    /**
     * 转换桶
     * 
     * @param srcObj
     *            源对象
     * 
     * @param taObj
     *            目标对象, 如果为空，则相当于将源对象在两个桶管理器直接直接写
     * @throws IOException
     * @throws WnIoHandleMutexException
     */
    void transBM(WnObj srcObj, WnObj taObj) {
        if (null == taObj)
            taObj = srcObj;

        WnIoHandle h0 = null, h1 = null;

        try {
            // 打开源柄
            h0 = tc.fromBM.open(srcObj, Wn.Io.R, tc.fromIndexer);

            // 打开标柄
            h1 = tc.toBM.open(taObj, Wn.Io.W, tc.toIndexer);

            // 准备缓冲区
            byte[] buf = new byte[tc.bufferSize];

            // 读写
            int len;
            while ((len = h0.read(buf)) >= 0) {
                if (len > 0) {
                    h1.write(buf, 0, len);
                }
            }

            // 搞定，刷新一下写句柄
            h1.flush();

        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
        // 确保关闭
        finally {
            Streams.safeClose(h0);
            Streams.safeClose(h1);
        }
    }
}
