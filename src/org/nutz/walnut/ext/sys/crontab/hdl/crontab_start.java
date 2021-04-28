package org.nutz.walnut.ext.sys.crontab.hdl;

import org.nutz.walnut.ext.sys.crontab.WnCronService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class crontab_start implements JvmHdl {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		if (!WnCronService.me.isAlive()) {
			WnCronService.me.startAtEs();
		}
	}
}
