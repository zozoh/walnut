package com.site0.walnut.core.bm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import com.site0.walnut.util.Wfiles;
import org.nutz.lang.Streams;
import com.site0.walnut.log.WnLog;

import com.site0.walnut.util.Wlog;

public abstract class WnIoWriteSwapHandle extends WnIoWriteHandle {

    private static final WnLog log = Wlog.getIO();

    /**
     * 交换文件
     */
    protected File swap;

    protected abstract File createSwapFile();

    @Override
    protected OutputStream getOutputStream() throws FileNotFoundException {
        if (null == swap) {
            swap = createSwapFile();
        }
        return Streams.chanOps(swap, false);
    }

    @Override
    public void on_close() throws IOException {
        // 根据交换文件更新对象的索引
        try {
            update_when_close(swap);
        }
        // 确保删除交换文件
        finally {
            try {
                if (null != swap && swap.exists()) {
                    Wfiles.deleteFile(swap);
                }
            }
            catch (Throwable e) {
                log.warn("IoW: Fail to delete SwapFile: %s" + swap, e);
            }
            finally {
                if (log.isDebugEnabled())
                    log.debugf("IoW: OK for delete SwapFile: %s", swap);
            }
        }

        // 重置成员
        swap = null;
    }

    protected abstract void update_when_close(File swap) throws IOException;

}
