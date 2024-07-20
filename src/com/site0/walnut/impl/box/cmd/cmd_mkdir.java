package com.site0.walnut.impl.box.cmd;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Files;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_mkdir extends JvmExecutor {

	@Override
	public void exec(WnSystem sys, String[] args) {

		ZParams params = ZParams.parse(args, "pocnql", "^(noexists)$");

		// 至少要有一个值
		if (params.vals.length == 0) {
			throw Er.create("e.cmd.mkdir.nopath");
		}

		boolean noexists = params.is("noexists");

		// 准备输出
		List<WnObj> outs = new LinkedList<WnObj>();

		// 得到目标路径
		for (String ph : params.vals) {
			String path = Wn.normalizeFullPath(ph, sys);
			WnObj o;
			// 如果是创建父
			if (params.has("p")) {
				if (noexists) {
					o = sys.io.createIfNoExists(null, path, WnRace.DIR);
				} else {
					o = sys.io.create(null, path, WnRace.DIR);
				}

			}
			// 不能自动创建父
			else {
				String pph = Files.getParent(path);
				WnObj pobj = sys.io.check(null, pph);
				String nm = Files.getName(path);
				if (noexists) {
					o = sys.io.createIfNoExists(pobj, nm, WnRace.DIR);
				} else {
					o = sys.io.create(pobj, nm, WnRace.DIR);
				}
			}
			// 计入结果
			outs.add(o);
		}

		// 输出
		if (params.is("o")) {
			Cmds.output_objs(sys, params, null, outs, true);
		}
	}

}
