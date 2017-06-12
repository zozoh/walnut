package org.nutz.walnut.ext.backup.hdl;

import java.util.Arrays;
import java.util.HashSet;

import org.nutz.lang.Lang;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.backup.BackupDumpContext;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("^(debug|v|trace|keep|dry)$")
public class backup_dump extends backup_xxx implements JvmHdl {
	
	@Override
	public void invoke(WnSystem sys, JvmHdlContext hc) {
		BackupDumpContext conf = new BackupDumpContext();
		// 如果指定了配置信息的来源,读取之
		if (hc.params.has("conf")) {
			WnObj wobj = sys.io.check(null, Wn.normalizeFullPath(hc.params.get("conf"), sys));
			// 先看看是否有backup_config属性嘛,
			if (wobj.has("backup_config")) {
				conf = Lang.map2Object(wobj.getAs("backup_config", NutMap.class), BackupDumpContext.class);
			} else {
				// 木有,那就当文本读咯
				conf = sys.io.readJson(wobj, BackupDumpContext.class);
				if (conf == null) {
					sys.err.print("null config!!!");
					return;
				}
			}
		} else {
			// 先读取基准路径
			conf.base = hc.params.get("base", "/");
			// 然后就是需要备份导出的路径
			conf.includes = Arrays.asList(hc.params.vals);
			if (conf.includes.isEmpty()) { // 必须带一个!!
				sys.err.print("aleast one path for dump!!");
				return;
			}
			// 是不是要精确匹配特定的路径模式呢??
			if (hc.params.has("includePatterns")) {
				conf.includePatterns = Arrays.asList(hc.params.get("includePatterns").split(","));
			}
			// 是否需要排除某些路径呢?
			if (hc.params.has("excludes")) {
				conf.excludes = Arrays.asList(hc.params.get("excludes").split(","));
			}
			// 是否需要排除特定的路径模式呢?
			if (hc.params.has("excludePatterns")) {
				conf.excludePatterns = Arrays.asList(hc.params.get("excludePatterns").split(","));
			}
			// 更多日志输出
			conf.debug = hc.params.is("debug") || hc.params.is("v") || hc.params.is("trace");
			// 不生成最终的压缩包吗?
			conf.dry = hc.params.is("dry");
			// 最终压缩包的格式是怎样的? 当前只有zip
			conf.outputFormat = hc.params.get("outputFormat", "zip");
			// 是否保留临时文件夹
			conf.keepTemp = "true".equals(hc.params.get("keepTemp"));
			// 目标路径,通常不需要指定
			conf.dst = hc.params.get("dst");
			if (conf.dst == null)
				conf.dst = Wn.normalizeFullPath("~/.dump/" + R.UU32() + conf.outputFormat, sys);
			// 指定之前的备份包,那么只生成增量包
			if (hc.params.has("prevs"))
				conf.prevs = Arrays.asList(hc.params.get("prevs"));
		}
		conf.se = sys.se;
		conf.sys = sys;
		conf.sha1Set = new HashSet<>();
		conf.log = sys.getLog(hc.params);
        dump(conf, sys, hc.params);
	}

}
