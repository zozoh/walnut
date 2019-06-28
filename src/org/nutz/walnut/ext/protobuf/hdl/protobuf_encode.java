package org.nutz.walnut.ext.protobuf.hdl;

import java.io.OutputStream;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.protobuf.ProtobufPool;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

import com.google.protobuf.AbstractMessage;

@JvmHdlParamArgs("cqn")
public class protobuf_encode implements JvmHdl {
    
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String className = hc.params.val_check(0);
        // protobuf 的对象并不适合fromJson/toJson,非常蛋疼
        NutMap map = Json.fromJson(NutMap.class, sys.in.getReader());
        Object obj = ProtobufPool.fromMap(ProtobufPool.getClass(className), map);
        if (hc.params.has("f")) {
            // 从文件读取
            String path = Wn.normalizeFullPath(hc.params.get("f"), sys);
            WnObj wobj = sys.io.createIfNoExists(null, path, WnRace.FILE);
            try (OutputStream out = sys.io.getOutputStream(wobj, 0)) {
                ((AbstractMessage)obj).writeTo(out);
            }
        }
        else{
            ((AbstractMessage)obj).writeTo(sys.out.getOutputStream());
        }
    }

}
