package com.site0.walnut.impl.box.cmd;

import org.nutz.json.Json;
import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;

/**
 * 打印系统内存使用情况
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class cmd_jvmMemory extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        NutMap map = new NutMap();
        Runtime rt = Runtime.getRuntime();
        map.put("free", Ws.formatSizeForReadBy1024(rt.freeMemory()));
        map.put("max", Ws.formatSizeForReadBy1024(rt.maxMemory()));
        map.put("total", Ws.formatSizeForReadBy1024(rt.totalMemory()));

        sys.out.println(Json.toJson(map));
    }

}
