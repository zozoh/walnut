package org.nutz.walnut.ext.backup.hdl;

import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.backup.WnImpExp;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

@JvmHdlParamArgs("^(debug|v|trace|keep|dry)$")
public class backup_restore implements JvmHdl {
	
	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) {
		ZParams params = hc.params;
        WnImpExp impexp = Mvcs.getIoc().get(WnImpExp.class);
        impexp.imp(params.val_check(0), params.val_check(1), params, sys.getLog(params));
	}
}
