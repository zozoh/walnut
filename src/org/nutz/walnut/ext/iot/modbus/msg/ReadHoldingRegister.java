package org.nutz.walnut.ext.iot.modbus.msg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.nutz.walnut.ext.iot.modbus.Modbus;
import org.nutz.walnut.ext.iot.modbus.ModbusRtuOpt;

@ModbusRtuOpt(value = 3, desc = "读保持寄存器")
// 发: 03030003000CB42D
// 收: 0303180001000100000000000000000000000000000000000000004efc
public class ReadHoldingRegister implements ModbusMsg {

    public int dev_addr;
    public int opt = 3;
    public int register_addr_start;
    public int size;
    public ArrayList<Integer> register_value;

    public byte[] encode() throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bao.write(dev_addr);
        bao.write(opt);
//        if (register_addr_start > 0) {
            bao.write(register_addr_start >> 8);
            bao.write(register_addr_start & 0xFF);
            bao.write(size >> 8);
            bao.write(size & 0xFF);
//        } else {
//            // 事实上不会进这里才对
//            bao.write(size);
//            for (int value : register_value) {
//                bao.write(value >> 8);
//                bao.write(value & 0xFF);
//            }
//        }
        byte[] buf = bao.toByteArray();
        byte[] crc = Modbus.getCrc(buf, buf.length);
        bao.write(crc);
        return bao.toByteArray();
    }

    public ReadHoldingRegister decode(byte[] buf) throws IOException {
        ReadHoldingRegister re = new ReadHoldingRegister();
        re.dev_addr = buf[0];
        re.opt = buf[1];
        re.size = buf[2] & 0xFF;

        ArrayList<Integer> values = new ArrayList<>(re.size / 2);
        for (int i = 0; i < re.size/2; i++) {
            int H = buf[3 + i * 2] & 0xFF;
            int L = buf[3 + i * 2 + 1] & 0xFF;
            values.add((H << 8) + L);
        }
        re.register_value = values;
        return re;
    }

//    public static void main(String[] args) throws IOException {
//        // 03030003000CB42D
//        ReadHoldingRegister re = new ReadHoldingRegister();
//        re.dev_addr = 3;
//        re.opt = 3;
//        re.register_addr_start = 0x03;
//        re.size = 0x0C; // 12
//        byte[] buf = re.encode();
//        System.out.println(Lang.fixedHexString(buf).toUpperCase());
//        // 0303180001000100000000000000000000000000000000000000004efc
//        String data = "0303180001000100000000000000000000000000000000000000004efc";
//        buf = new byte[data.length() / 2];
//        for (int i = 0; i < buf.length; i++) {
//            buf[i] = (byte) Integer.parseInt(data.substring(i * 2, i * 2 + 2), 16);
//        }
//        re = re.decode(buf);
//        System.out.println(Json.toJson(re));
//    }
}
