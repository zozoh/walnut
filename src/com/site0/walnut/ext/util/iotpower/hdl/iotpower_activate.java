package com.site0.walnut.ext.util.iotpower.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.iot.modbus.Modbus;
import com.site0.walnut.ext.util.btea.BTea;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

import com.alibaba.druid.util.HexBin;

public class iotpower_activate implements JvmHdl {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		NutMap params = Json.fromJson(NutMap.class, sys.in.getReader());
		
		NutMap ret = new NutMap();
		
		String _uuid = params.getString("devid"); // 设备id,为24字节长的hex字符串
		int random = params.getInt("rint", 0x20210629);    // 随机数,默认值0x20210629
		
		int[] mU = BTea.toInt32(HexBin.decode(_uuid));
		
		int uuid[] = new int[4];
		uuid[0] = (int) ((mU[0] ^ random) - (0x000000FF&(random)));
		uuid[1] = (int) ((mU[1] ^ random) - (0x000000FF&(random >> 8)));
		uuid[2] = (int) ((mU[2] ^ random) - (0x000000FF&(random >> 16)));
		uuid[3] = (int) (random- (0x000000FF&(random >> 24)));
		
		byte[] U16 = new byte[16];
		for (int i = 0; i < 4; i++) {
			U16[i*4+3] = (byte) (uuid[i] & 0xFF);
			U16[i*4+2] = (byte) ((uuid[i]>>8) & 0xFF);
			U16[i*4+1] = (byte) ((uuid[i]>>16) & 0xFF);
			U16[i*4+0] = (byte) ((uuid[i]>>24) & 0xFF);
		}
		
		ret.setv("devsecret", HexBin.encode(U16));

		if (params.has("devsecret")) {
			//String _devid16 = params.getString("devsecret");
			byte[] crc16 = Modbus.getCrc(U16, 16);
			int r = random;
			r += ((crc16[1] & 0xFF) << 8) + (crc16[0]&0xFF);
			String code = String.format("%08X%08X", r & 0xFFFFFFFF, random & 0xFFFFFFFF);
			ret.setv("devcode", code);
		}

		sys.out.writeJson(ret, JsonFormat.full());
	}

	
//	public static void main(String[] args) {
//		String trueUUID = "0072002B0947303032333230";
//		int random = 0x20210629;
//		int[] k = BTea.toInt32("Luat IotPower-5V".getBytes());
//		{
//			byte[] myUUID = HexBin.decode(trueUUID);
//			//System.out.println(myUUID.length);
//			int mU[] = BTea.toInt32(myUUID);
//			int uuid[] = new int[4];
//			uuid[0] = (int) ((mU[0] ^ random) - (0x000000FF&(random)));
//			uuid[1] = (int) ((mU[1] ^ random) - (0x000000FF&(random >> 8)));
//			uuid[2] = (int) ((mU[2] ^ random) - (0x000000FF&(random >> 16)));
//			uuid[3] = (int) (random- (0x000000FF&(random >> 24)));
//
//			System.out.println("btea加密前");
//			System.out.printf("%08X\n", uuid[0] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[1] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[2] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[3] & 0xFFFFFFFF);
//			
//			byte[] U16 = new byte[16];
//			for (int i = 0; i < 4; i++) {
//				U16[i*4+3] = (byte) (uuid[i] & 0xFF);
//				U16[i*4+2] = (byte) ((uuid[i]>>8) & 0xFF);
//				U16[i*4+1] = (byte) ((uuid[i]>>16) & 0xFF);
//				U16[i*4+0] = (byte) ((uuid[i]>>24) & 0xFF);
//			}
//			BTea.btea(uuid, 4, k);
//			
////			byte[] kkkk = "Luat IotPower-5V".getBytes();
//			
////			System.out.printf("key[0] %08X\n", k[0] & 0xFFFFFFFF);
////			System.out.printf("kkkk[0] %02X%02X%02X%02X\n", kkkk[0] & 0xFF, kkkk[1]& 0xFF, kkkk[2]& 0xFF, kkkk[3]& 0xFF);
////			System.out.printf("key[0] %08X\n", k[1] & 0xFFFFFFFF);
////			System.out.printf("key[0] %08X\n", k[2] & 0xFFFFFFFF);
////			System.out.printf("key[0] %08X\n", k[3] & 0xFFFFFFFF);
//			
//			System.out.println("btea加密后");
//			String dev16 = String.format("%08X%08X%08X%08X", 
//					uuid[0] & 0xFFFFFFFF, uuid[1] & 0xFFFFFFFF, uuid[2] & 0xFFFFFFFF, uuid[3] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[0] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[1] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[2] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[3] & 0xFFFFFFFF);
//			System.out.println("QueryString == " + dev16);
//			
//			BTea.btea(uuid, -4, k);
//			
//			System.out.println("btea解密后");
//			System.out.printf("%08X\n", uuid[0] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[1] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[2] & 0xFFFFFFFF);
//			System.out.printf("%08X\n", uuid[3] & 0xFFFFFFFF);
//			
//			byte[] crc16 = Modbus.getCrc(BTea.fromInt32(uuid), 16);
//			
//			System.out.println("crc16 " + HexBin.encode(crc16));
//			
//			int r = 0x12345678;
//			int rnow = r;
//			//int w = 0x20212382; int y = 0x20210629;
//			//System.out.println(HexBin.encode(crc16));
//			r += ((crc16[1] & 0xFF) << 8) + (crc16[0]&0xFF);
//			System.out.printf(">>>> %08X%08X\n", r, rnow);
//			
//			//System.out.printf("%04X\n", w - y);
//			
//			
//			
////			int[] v2 = {1,2,3,4};
////			int[] k2 = {123, 1, 1, 4};
////			BTea.btea(v2, -4, k2);
////			System.out.println("错误密码测试---");
////			System.out.printf("%08X\n", v2[0] & 0xFFFFFFFF);
////			System.out.printf("%08X\n", v2[1] & 0xFFFFFFFF);
////			System.out.printf("%08X\n", v2[2] & 0xFFFFFFFF);
////			System.out.printf("%08X\n", v2[3] & 0xFFFFFFFF);
//		}
//		
//	}
}
