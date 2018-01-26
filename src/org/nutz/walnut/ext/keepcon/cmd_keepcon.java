package org.nutz.walnut.ext.keepcon;

import java.util.Date;

import org.nutz.lang.Lang;
import org.nutz.lang.Times;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

/**
 * 不停的向流里写会当前时间，保持网页连接
 * 
 * @author pw
 *
 */
public class cmd_keepcon extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        int keepMax = params.getInt("k", 10);
        int keepInterval = params.getInt("s", 10);
        int keepCount = 0;
        sys.out.printlnf("max: %d intraval: %ds", keepMax, keepInterval);
        while (keepCount < keepMax) {
            Lang.quiteSleep(keepInterval * 1000);
            keepCount++;
            sys.out.printlnf("No.%d Time:%s", keepCount, Times.sDTms2(new Date()));
        }
    }

}
