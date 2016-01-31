package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 修改密码
 * @author wendal
 *
 */
public class cmd_passwd extends JvmExecutor {

	public void exec(WnSystem sys, String[] args) throws Exception {
		if (args.length != 1)
			return;
		String passwd = Strings.trim(args[0]);
		if (passwd.length() < 4) {
			sys.err.print("must > 4");
			return;
		}
		// 为啥只有root能执行呢?
		sys.usrService.setPassword(sys.se.me(), passwd);
	}

}
