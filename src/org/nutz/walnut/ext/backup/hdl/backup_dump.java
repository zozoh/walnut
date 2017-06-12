package org.nutz.walnut.ext.backup.hdl;

import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.backup.WnImpExp;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

@JvmHdlParamArgs("^(debug|v|trace|keep|dry)$")
public class backup_dump implements JvmHdl {
	
	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) {
		String root = hc.params.val_check(0);
		ZParams params = hc.params;
        
        // 先暴力取一下吧
        WnImpExp impExp = Mvcs.getIoc().get(WnImpExp.class);
        impExp.exp(root, params, sys.getLog(params), sys.se);
	}

}
