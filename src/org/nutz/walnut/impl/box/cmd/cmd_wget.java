package org.nutz.walnut.impl.box.cmd;

import java.util.Arrays;
import java.util.List;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

@IocBean
public class cmd_wget extends JvmExecutor {

	public void exec(WnSystem sys, String[] _args) {
	    List<String> args = Arrays.asList(_args);
		if (args.isEmpty()) {
			throw Er.create("e.cmd.noargs");
		}
		String target = null;
		String url = args.get(0);
		if (args.size() == 3) {
			if (!args.get(0).equals("-O")) {
				throw Er.create("e.cmd.badargs");
			}
			target = args.get(1);
			url = args.get(2);
		} else {
			if (url.contains("?"))
				target = url.substring(url.lastIndexOf("/")+1, url.indexOf("?"));
			else
				target = url.substring(url.lastIndexOf("/")+1);
		}
		if (!url.startsWith("http")) {
			url = "http://" + url;
		}
		if (target.contains("/")) {
			sys.out.println("!only support download to current dir");
			return;
		}
		WnObj p = getCurrentObj(sys); 
		Response resp = Http.get(url);
		if (!resp.isOK()) {
			throw Er.create("e.cmd.badargs");
		}
		int sz = resp.getHeader().getInt("Content-Length", -1);
		if (sz > -1) {
			sys.out.println("size=" + sz);
		}
		WnObj obj = sys.io.createIfNoExists(p, target, WnRace.FILE);
		sys.io.writeAndClose(obj, resp.getStream());
		sys.out.println("done");
	}

}