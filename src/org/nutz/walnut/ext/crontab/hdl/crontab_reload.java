package org.nutz.walnut.ext.crontab.hdl;

import org.nutz.walnut.ext.crontab.WnCronService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class crontab_reload implements JvmHdl {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnCronService.me.reload();
	}

}
