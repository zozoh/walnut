package org.nutz.walnut.ext.weather.hdl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs(value = "cqn")
public class weather_gaode implements JvmHdl {

	protected String base = "https://restapi.amap.com/v3/weather/weatherInfo";
	protected String key = "d223975148386c08620740e840702da2";

	protected Map<String, String> cityCodes;

	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
		String city = hc.params.val_check(0);
		String type = hc.params.get("tp", "base");
		String key = hc.params.get("k", this.key);
		String cityCode = city;
		if (Strings.isBlank(cityCode)) {
			cityCode = "440100";
		} else if (!Strings.isNumber(city)) {
			if (cityCodes == null) {
				InputStream ins = getClass().getClassLoader().getResourceAsStream("AMap_adcode_citycode.csv");
				BufferedReader br = new BufferedReader(Streams.utf8r(ins));
				Map<String, String> cityCodes = new HashMap<>();
				while (br.ready()) {
					String line = br.readLine();
					if (line == null)
						break;
					String[] tmp = line.split(",");
					cityCodes.put(tmp[0], tmp[1]);
				}
				this.cityCodes = cityCodes;
			}
			cityCode = this.cityCodes.get(city);
			if (cityCode == null) {
				cityCode = this.cityCodes.get(city + "市");
				if (cityCode == null) {
					cityCode = this.cityCodes.get(city + "县");
				}
				if (cityCode == null) {
					cityCode = this.cityCodes.get(city + "区");
				}
			}
			if (cityCode == null) {
				cityCode = "440100"; // 默认广州吧
			}
		}
		Response resp = Http.get(base, new NutMap("key", key).setv("city", cityCode).setv("extensions", type), 3000);
		if (resp.isOK()) {
			NutMap re = Json.fromJson(NutMap.class, resp.getReader());
			if (re.getInt("status", 1) == 1) {
				sys.out.writeJson(re, Cmds.gen_json_format(hc.params));
			} else {
				sys.err.print("e.cmd.weather.code_" + re.get("code"));
			}
			return;
		} else {
			sys.err.print("e.cmd.weather.recode_" + resp.getStatus());
		}
	}
}
