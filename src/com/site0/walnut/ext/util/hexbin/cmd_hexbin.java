package com.site0.walnut.ext.util.hexbin;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_hexbin extends JvmExecutor {

	@Override
	public void exec(WnSystem sys, String[] args) throws Exception {
		ZParams params = ZParams.parse(args, "dn");
		// 得到内容
		byte[] data = null;
		if (params.vals.length > 0) {
			data = params.vals[0].getBytes();
		} else if (sys.pipeId > 0) {
			data = Streams.readBytes(sys.in.getInputStream());
		} else {
			sys.err.print("e.cmd.base64.need_input");
			return;
		}
		byte[] result;
		if (params.is("d")) {
			result = new byte[data.length / 2];
			for (int i = 0; i < data.length; i+=2) {
				result[i/2] = (byte) Integer.parseInt(new String(data, i, 2), 16);
			}
		} else {
			result = Wlang.fixedHexString(data).getBytes();
		}
		if (result != null)
			sys.out.write(result);
	}

}
