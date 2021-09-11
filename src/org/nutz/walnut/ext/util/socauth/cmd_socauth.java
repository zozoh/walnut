package org.nutz.walnut.ext.util.socauth;

import org.nutz.lang.Lang;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_socauth extends JvmExecutor {

	@Override
	public void exec(WnSystem sys, String[] args) throws Exception {
		ZParams params = ZParams.parse(args, null);
		String drv_id = params.val_check(0);
		String phone = "13416121384";
		if (params.vals.length > 1)
			phone = params.vals[1];
		StringBuilder sb = Lang.execOutput(new String[] {"/usr/local/bin/soc_auth_v1", drv_id, phone});
		if (sb != null) {
			sys.out.print(sb);
		}
		else {
			sys.err.print("exec error");
		}
	}

}
