package org.nutz.walnut.ext.protobuf.hdl;

import java.io.OutputStream;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.protobuf.ProtobufPool;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

import com.google.protobuf.Message;

@JvmHdlParamArgs("cqn")
public class protobuf_encode implements JvmHdl {
    
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String className = hc.params.val_check(0);
        Message.Builder builder = (Message.Builder)ProtobufPool.getClass(className).getMethod("newBuilder").invoke(null);
        com.google.protobuf.util.JsonFormat.parser().merge(sys.in.getReader(), builder);
        Message msg = builder.build();
        if (hc.params.has("f")) {
            // 从文件读取
            String path = Wn.normalizeFullPath(hc.params.get("f"), sys);
            WnObj wobj = sys.io.createIfNoExists(null, path, WnRace.FILE);
            try (OutputStream out = sys.io.getOutputStream(wobj, 0)) {
                msg.writeTo(out);
            }
        }
        else{
            msg.writeTo(sys.out.getOutputStream());
        }
    }

}
