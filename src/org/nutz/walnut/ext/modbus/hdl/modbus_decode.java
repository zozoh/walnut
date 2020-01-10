package org.nutz.walnut.ext.modbus.hdl;

import java.io.ByteArrayOutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.ext.modbus.Modbus;
import org.nutz.walnut.ext.modbus.msg.ModbusMsg;
import org.nutz.walnut.ext.modbus.msg.ReadHoldingRegister;
import org.nutz.walnut.ext.modbus.msg.WriteSingleRegister;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value="cqn")
public class modbus_decode implements JvmHdl {
    
    private static final Log log = Logs.get();

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
            String tmp = hc.params.val(0).replace(" ", "").trim();
            if (tmp.length() % 2 != 0) {
                sys.err.print("e.cmd.modbus.decode.bad_hex_string");
                return;
            }
            buf = new byte[tmp.length() / 2];
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) Integer.parseInt(tmp.substring(i * 2, i * 2 + 2), 16);
            }
        }
        else {
            sys.err.print("e.cmd.modbus.decode.need_data");
            return;
        }
        // 校验CRC
        byte[] msg = buf;
        byte[] tmp = Modbus.getCrc(msg, msg.length - 2);
        byte[] act = new byte[] {msg[msg.length - 2], msg[msg.length - 1]};
        if (tmp[0] != act[0] || tmp[1] != act[1]) {
            log.debugf("modbus crc NOT match, expect %s but %s", Lang.fixedHexString(tmp), Lang.fixedHexString(act));
            sys.err.print("e.cmd.modbus.decode.bad_crc");
            return;
        }
        int opt = msg[1];
        ModbusMsg m = null;
        switch (opt) {
        case 3:
            m = new ReadHoldingRegister().decode(msg);
            break;
        case 6:
            m = new WriteSingleRegister().decode(msg);
            break;
        default:
            break;
        }
        if (m == null) {
            sys.err.print("e.cmd.modbus.decode.not_support_opt");
        }
        else {
            sys.out.writeJson(m, Cmds.gen_json_format(hc.params));
        }
    }
}
