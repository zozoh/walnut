package com.site0.walnut.ext.iot.modbus.msg;

import java.io.IOException;

public interface ModbusMsg {

    public byte[] encode() throws IOException;

    public ModbusMsg decode(byte[] msg) throws IOException;

}
