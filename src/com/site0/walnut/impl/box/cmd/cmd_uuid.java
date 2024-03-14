package com.site0.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_uuid extends JvmExecutor {

	public void exec(WnSystem sys, String[] args) throws Exception {
		ZParams params = ZParams.parse(args, "d");
		String format = params.get("F", "uu32");
		int count = params.getInt("c", 1);
		String f = params.get("f");
		List<String> list = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			UUID uuid = UUID.randomUUID();
			String result = null;
			switch (format) {
			case "uu32":
			case "32":
				result = R.UU32(uuid);
				break;
			case "uu16":
			case "16":
				result = R.UU16(uuid);
				break;
			case "uu64":
			case "64":
				result = R.UU64(uuid);
				break;
			case "STR":
				result = uuid.toString();
				break;
			default:
				sys.err.println("unsupport format");
				return;
			}
			list.add(result);
		}
		if (f == null)
			sys.out.print(Strings.join("\r\n", list.toArray()));
		else {
			f = Wn.normalizeFullPath(f, sys);
			WnObj obj = sys.io.createIfNoExists(null, f, WnRace.FILE);
			sys.io.writeText(obj, Strings.join("\r\n", list.toArray()));
		}
	}
}
