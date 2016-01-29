package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 列出用户所在的组
 * @author wendal
 *
 */
public class cmd_groups extends JvmExecutor {

	public void exec(WnSystem sys, String[] args) throws Exception {
		WnObj grpDir = sys.io.check(null, "/sys/grp");
		WnUsr usr = args.length > 0 ? sys.usrService.check(args[0]) : sys.me;
		sys.io.each(Wn.Q.pid(grpDir.id()), (index,child,length)->{
			if (sys.io.exists(child, "people/"+usr.id()))
				sys.out.println(child.name());
		});
	}

}
