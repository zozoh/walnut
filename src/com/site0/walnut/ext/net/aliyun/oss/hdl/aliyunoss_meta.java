package com.site0.walnut.ext.net.aliyun.oss.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.aliyun.oss.WnAliyunOssService;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class aliyunoss_meta extends aliyunoss_xxx {

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		WnAliyunOssService oss = getService(sys, hc);
		if (hc.params.has("u")) {
			// 更新操作
			NutMap u_map = null;
			String json = hc.params.get("u");
			// 标准输入里读取
			if ("true".equals(json)) {
				json = sys.in.readAll();
			}
			// 解析 Map
			try {
				u_map = Lang.map(json);
			} catch (Exception e) {
				u_map = new NutMap();
			}
			if (u_map.isEmpty()) {
				return;
			}
			for (String objectName : hc.params.vals) {
				oss.setMeta(objectName, u_map);
			}
		}
		// 读取操作
		sys.out.println(Json.toJson(oss.getMeta(hc.params.val_check(0)), hc.jfmt));
	}

}
