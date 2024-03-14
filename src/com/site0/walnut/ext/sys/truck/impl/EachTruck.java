package com.site0.walnut.ext.sys.truck.impl;

import java.io.IOException;

import org.nutz.lang.Each;
import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.WnIoHandle;
import com.site0.walnut.core.WnIoHandleMutexException;
import com.site0.walnut.ext.sys.truck.TruckContext;
import com.site0.walnut.util.Wn;

abstract class EachTruck implements Each<WnObj> {

    private TruckContext tc;

    EachTruck(TruckContext tc) {
        this.tc = tc;
    }

    void appendObj(WnObj obj) {
        // 打印
        if (null != tc.printer) {
            tc.printer.print(obj);
        }
        // 计入列表
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
        // 检查是否存在
        if (tc.noexists) {
            if (tc.io.exists(tc.toDir, obj.name())) {
                return tc.io.fetch(tc.toDir, obj.name());
            }
        }
        // 要求生成新的 ID
        if (tc.genId) {
            obj.id(Wn.genId());
        }
        // 直接插入
        return tc.io.create(tc.toDir, obj);
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
            h1 = tc.io.openHandle(taObj, Wn.Io.W);

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
