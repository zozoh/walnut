package com.site0.walnut.ext.iot.modbus.hdl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.iot.modbus.msg.ReadHoldingRegister;
import com.site0.walnut.ext.iot.modbus.msg.WriteSingleRegister;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs(regex="^hex$")
public class modbus_encode implements JvmHdl {
    
    //private static final Log log = Wlog.getCMD();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        byte[] buf = null;
        if (sys.pipeId > 0) {
            // 从输入流读取
            buf = Streams.readBytesAndClose(sys.in.getInputStream());
        }
        else if (hc.params.has("f")) {
            // 从文件读取
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            sys.io.readAndClose(sys.io.check(null, Wn.normalizeFullPath(hc.params.get("f"), sys.session)), bao);
            buf = bao.toByteArray();
        }
        else if (hc.params.vals.length > 0){
            // 从命令行参数读取, hex字符串形式
            buf = hc.params.vals[0].getBytes();
        }
        else {
            sys.err.print("e.cmd.modbus.encode.need_data");
            return;
        }
        NutMap re = Json.fromJson(NutMap.class, new String(buf));
        if (!re.containsKey("opt")) {
            sys.err.print("e.cmd.modbus.encode.need_data.opt");
            return;
        }
        int opt = re.getInt("opt");
        buf = null;
        switch (opt) {
        case 3:
            buf = Lang.map2Object(re, ReadHoldingRegister.class).encode();
            break;
        case 6:
            buf = Lang.map2Object(re, WriteSingleRegister.class).encode();
            break;
        default:
            break;
        }
        if (buf == null) {
            sys.err.print("e.cmd.modbus.encode.opt_not_support");
            return;
        }
        if (hc.params.is("hex")) {
            sys.out.print(Lang.fixedHexString(buf).toUpperCase());
        }
        else {
            if (hc.params.has("f")) {
                String path = Wn.normalizeFullPath(hc.params.get("f"), sys.session);
                WnObj wobj = sys.io.createIfNoExists(null, path, WnRace.FILE);
                sys.io.writeAndClose(wobj, new ByteArrayInputStream(buf));
            }
            else {
                sys.out.write(buf);
            }
        }
    }
}
