package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_adduser extends JvmExecutor {

	public void exec(WnSystem sys, String[] args) throws Exception {
		if (args.length < 1)
            return;
		String name = Strings.trim(args[0]);
		String password = args.length > 1 ? Strings.trim(args[1]) : "123456";
		sys.usrService.create(name, password);
	}

}
