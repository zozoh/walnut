package org.nutz.walnut.ext.iot.protobuf.hdl;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.reflect.FastClassFactory;
import org.nutz.lang.reflect.FastMethod;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.iot.protobuf.ProtobufPool;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.Webs.Err;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

public class protobuf_decode implements JvmHdl {

    protected static final Map<String, FastMethod> parser = new HashMap<>();
    
    private static JsonFormat.Printer printer = JsonFormat.printer().preservingProtoFieldNames().includingDefaultValueFields();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String className = hc.params.val_check(0);
        FastMethod fm = parser.get(className);
        if (fm == null) {
            Method method = ProtobufPool.getClass(className).getMethod("parseFrom", InputStream.class);
            fm = FastClassFactory.get(method);
            parser.put(className, fm);
        }
        Object obj = null;
        if (hc.params.has("f")) {
            // 从文件读取
            String path = Wn.normalizeFullPath(hc.params.get("f"), sys);
            WnObj wobj = sys.io.check(null, path);
            try (InputStream ins = sys.io.getInputStream(wobj, 0)) {
                obj = fm.invoke(null, ins);
            }
        } else if (sys.pipeId > 0) {
            obj = fm.invoke(null, sys.in.getInputStream());
        } else {
            throw Err.create("e.cmd.protobuf.decode.need_datas");
        }
        // 输出内容
        sys.out.print(printer.print((Message)obj));
    }

}
