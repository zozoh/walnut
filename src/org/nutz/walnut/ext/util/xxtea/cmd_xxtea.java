package org.nutz.walnut.ext.util.xxtea;

import org.nutz.lang.Streams;
import org.nutz.repo.Base64;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_xxtea extends JvmExecutor {

	@Override
	public void exec(WnSystem sys, String[] args) throws Exception {
		ZParams params = ZParams.parse(args, "d");
		byte[] key = null;
		if (params.has("k")) {
			key = params.getString("k").getBytes();
		}
		else if (params.has("k64")) {
			key = Base64.decode(params.get("k64"));
		}
		else {
			sys.err.print("e.cmd.xxtea.need_key");
			return;
		}
		boolean isDecode = params.is("d");
		byte[] scr = Streams.readBytes(sys.in.getInputStream());
		byte[] dst = null;
		if (isDecode) {
			dst = XXTEA.decrypt(scr, key);
		}
		else {
			dst = XXTEA.encrypt(scr, key);
		}
		if (dst != null)
			sys.out.write(dst);
	}

}
