package org.nutz.walnut.ext.modbus;

import org.nutz.lang.Lang;

public class Modbus {
    
    public static byte[] fixCrc(byte[] bytes) {
        byte[] crc = getCrc(bytes, bytes.length -  2);
        bytes[bytes.length - 2] = crc[0];
        bytes[bytes.length - 1] = crc[1];
        return bytes;
    }

    public static byte[] getCrc(byte[] bytes, int len) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;

        int i, j;
        for (i = 0; i < len; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        return new byte[] {(byte)CRC, (byte)(CRC >> 8)};
    }
    
    public static String getCrcString(byte[] bytes, int len) {
        return Lang.fixedHexString(getCrc(bytes, len));
    }
}
