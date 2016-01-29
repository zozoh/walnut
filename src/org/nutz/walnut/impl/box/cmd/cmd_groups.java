package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 列出用户的密码
 * @author wendal
 *
 */
public class cmd_groups extends JvmExecutor {

	public void exec(WnSystem sys, String[] args) throws Exception {
		WnObj grpDir = sys.io.check(null, "/sys/grp");
		String selfId = sys.me.id();
		sys.io.each(Wn.Q.pid(grpDir.id()), (index,child,length)->{
			System.out.println(child);
			if (sys.io.exists(child, "people/"+selfId))
				sys.out.println(child.name());
		});
	}

}
