package com.site0.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class cmd_rename extends JvmExecutor {

	@Override
	public void exec(WnSystem sys, String[] args) throws Exception {
		ZParams params = ZParams.parse(args, "cqn", "^(keep|quiet)$");
		String newName;
		WnObj obj;
		boolean keepType = params.is("keep");

		String id = params.getString("id");
		if (!Ws.isBlank(id)) {
			newName = params.val_check(0);
			obj = sys.io.checkById(id);
		}
		// 根据路径读取
		else {
			String ph = params.val_check(0);
			newName = params.val_check(1);
			obj = Wn.checkObj(sys, ph);
		}

		// 是否需要改名？
		if (!obj.isSameName(newName)) {
			// 执行改名
			sys.io.rename(obj, newName, keepType);
		}

		// 输出
		if (!params.is("quiet")) {
			JsonFormat jfmt = Cmds.gen_json_format(params);
			String json = Json.toJson(obj, jfmt);
			sys.out.println(json);
		}

	}

}
