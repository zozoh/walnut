package com.site0.walnut.ext.sys.crontab.hdl;

import com.site0.walnut.ext.sys.crontab.WnCronService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class crontab_reload implements JvmHdl {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnCronService.me.reload();
	}

}
