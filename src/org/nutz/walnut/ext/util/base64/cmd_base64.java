package org.nutz.walnut.ext.util.base64;

import org.nutz.lang.Streams;
import org.nutz.repo.Base64;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_base64 extends JvmExecutor {

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
			result = Base64.decode(data);
		} else {
			result = Base64.encodeToByte(data, params.is("n"));
		}
		if (result != null)
			sys.out.write(result);
	}

}
