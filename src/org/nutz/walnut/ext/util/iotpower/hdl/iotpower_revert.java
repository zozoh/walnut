package org.nutz.walnut.ext.util.iotpower.hdl;

import org.nutz.walnut.ext.util.btea.BTea;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

import com.alibaba.druid.util.HexBin;

public class iotpower_revert implements JvmHdl {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		ZParams params = ZParams.parse(hc.args, null);

		String devsecret = params.getString("devsecret"); // 设备id,为24字节长的hex字符串

		byte[] U12 = revert(devsecret);

		sys.out.print(HexBin.encode(U12));
	}
	
	public static byte[] revert(String devsecret) {
		int random = 0;
		int[] mU = BTea.toInt32(HexBin.decode(devsecret));
		{
			// 反推随机数
			int lastR = mU[3];
			int forcheck = 0;
			for (int i = 0; i < 256; i++) {
				//System.out.printf("%08X %02X\n", lastR, i);
				forcheck = lastR + i;
				if (forcheck >>> 24 == i) {
					random = forcheck;
					//System.out.printf("found %02X %08X\n", i, forcheck);
					break;
				}
			}
		}

		int uuid[] = new int[3];
//		uuid[0] = (int) ((mU[0] ^ random) - (0x000000FF & (random)));
//		uuid[1] = (int) ((mU[1] ^ random) - (0x000000FF & (random >>> 8)));
//		uuid[2] = (int) ((mU[2] ^ random) - (0x000000FF & (random >>> 16)));
		// uuid[3] = (int) (random- (0x000000FF&(random >>> 24)));

		uuid[0] = (int) ((mU[0] + (0x000000FF & (random)) ^ random));
		uuid[1] = (int) ((mU[1] + (0x000000FF & (random >>> 8)) ^ random));
		uuid[2] = (int) ((mU[2] + (0x000000FF & (random >>> 16)) ^ random));

		byte[] U12 = new byte[12];
		for (int i = 0; i < 3; i++) {
			U12[i * 4 + 3] = (byte) (uuid[i] & 0xFF);
			U12[i * 4 + 2] = (byte) ((uuid[i] >>> 8) & 0xFF);
			U12[i * 4 + 1] = (byte) ((uuid[i] >>> 16) & 0xFF);
			U12[i * 4 + 0] = (byte) ((uuid[i] >>> 24) & 0xFF);
		}
		return U12;
	}
	
//	public static void main(String[] args) {
//		String cCode = "E9017B3FB081902E7666AB2F647982A9";
//		int[] mU = BTea.toInt32(HexBin.decode(cCode));
//		BTea.btea(mU, -4, BTea.toInt32("Luat IotPower-5V".getBytes()));
//		System.out.println(">> " + HexBin.encode(BTea.fromInt32(mU)));
//		System.out.println(HexBin.encode(revert(HexBin.encode(BTea.fromInt32(mU)))));
////		System.out.println(HexBin.encode(revert("B591D941A3AE8EF40C7D187D4D1EF032")));
//		// 0072002B0947303032333230
//		// 0072002B0947303032333230
//	}
}
