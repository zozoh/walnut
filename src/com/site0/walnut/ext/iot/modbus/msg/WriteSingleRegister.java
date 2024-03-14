package com.site0.walnut.ext.iot.modbus.msg;

import java.io.IOException;

import com.site0.walnut.ext.iot.modbus.Modbus;
import com.site0.walnut.ext.iot.modbus.ModbusRtuOpt;

@ModbusRtuOpt(value = 6, desc = "写入单个保持寄存器")
// 0306000a000169ea
// 0306000F0000B82B
public class WriteSingleRegister implements ModbusMsg {

    public int dev_addr;
    public int opt = 6;
    public int register_addr;
    public int register_value;

    @Override
    public byte[] encode() throws IOException {
        byte[] buf = new byte["0306000F0000B82B".length() / 2];
        buf[0] = (byte) dev_addr;
        buf[1] = (byte) opt;
        buf[2] = (byte) (register_addr >> 8);
        buf[3] = (byte) (register_addr & 0xFF);
        buf[4] = (byte) (register_value >> 8);
        buf[5] = (byte) (register_value & 0xFF);
        Modbus.fixCrc(buf);
        return buf;
    }

    @Override
    public ModbusMsg decode(byte[] msg) throws IOException {
        WriteSingleRegister re = new WriteSingleRegister();
        re.dev_addr = msg[0];
        re.opt = msg[1];
        re.register_addr = ((msg[2] & 0xFF) << 8) + (msg[3] & 0xFF);
        re.register_value = ((msg[4] & 0xFF) << 8) + (msg[5] & 0xFF);
        return re;
    }

//    public static void main(String[] args) throws IOException {
//        for (int i = 0; i < 2; i++) {
//            WriteSingleRegister re = new WriteSingleRegister();
//            re.dev_addr = 3;
//            re.opt = 6;
//            re.register_addr = 0x03 + i;
//            re.register_value = 0x01;
//            byte[] buf = re.encode();
//            System.out.println(Lang.fixedHexString(buf).toUpperCase());
//            re = (WriteSingleRegister) re.decode(buf);
//            if (re.dev_addr != 3) {
//                System.out.println("bad dev_addr : " + re.dev_addr);
//            }
//            if (re.opt != 6) {
//                System.out.println("bad opt: " + re.opt);
//            }
//            if (re.register_addr != 3+i) {
//                System.out.println("bad register_addr: " + re.register_addr);
//            }
//            if (re.register_value != 1) {
//                System.out.println("bad register_value: " + re.register_value);
//            }
//        }
//    }
}
