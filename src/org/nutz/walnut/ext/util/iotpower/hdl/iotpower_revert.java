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
		int random = params.getInt("rint", 0x20210629); // 随机数,默认值0x20210629

		int[] mU = BTea.toInt32(HexBin.decode(devsecret));

		int uuid[] = new int[3];
//		uuid[0] = (int) ((mU[0] ^ random) - (0x000000FF & (random)));
//		uuid[1] = (int) ((mU[1] ^ random) - (0x000000FF & (random >> 8)));
//		uuid[2] = (int) ((mU[2] ^ random) - (0x000000FF & (random >> 16)));
		// uuid[3] = (int) (random- (0x000000FF&(random >> 24)));

		uuid[0] = (int) ((mU[0] + (0x000000FF & (random)) ^ random));
		uuid[1] = (int) ((mU[1] + (0x000000FF & (random >> 8)) ^ random));
		uuid[2] = (int) ((mU[2] + (0x000000FF & (random >> 16)) ^ random));

		byte[] U12 = new byte[12];
		for (int i = 0; i < 3; i++) {
			U12[i * 4 + 3] = (byte) (uuid[i] & 0xFF);
			U12[i * 4 + 2] = (byte) ((uuid[i] >> 8) & 0xFF);
			U12[i * 4 + 1] = (byte) ((uuid[i] >> 16) & 0xFF);
			U12[i * 4 + 0] = (byte) ((uuid[i] >> 24) & 0xFF);
		}

		sys.out.print(HexBin.encode(U12));
	}
}
