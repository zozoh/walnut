package org.nutz.walnut.ext.sys.dsync.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.dsync.DSyncContext;
import org.nutz.walnut.ext.sys.dsync.DSyncFilter;
import org.nutz.walnut.ext.sys.dsync.bean.WnDataSyncItem;
import org.nutz.walnut.ext.sys.dsync.bean.WnDataSyncTree;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class dsync_ids extends DSyncFilter {
    
    

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, DSyncContext fc, ZParams params) {
        // 防守
        if (!fc.hasTrees()) {
            throw Er.create("e.cmd.dsync.ids.TreeNotLoaded");
        }
        // 分析参数
        WnMatch testItem = getItemMatch(params);

        int limit = params.getInt("limit", 0);
        int skip = params.getInt("skip", 0);
        JsonFormat jfmt = Cmds.gen_json_format(params);

        // 输出模式
        String as = params.getString("as", "path");
        if (!as.matches("^(id|path)$")) {
            throw Er.create("e.cmd.dsync.ids.InvalidAs", as);
        }
        boolean asId = "id".equals(as);

        // 过滤树
        String treeName = params.getString("tree", null);
        WnMatch testTree = AutoMatch.parse(treeName, true);

        // 循环收集
        NutMap re = new NutMap();
        int count = 0;
        for (WnDataSyncTree tree : fc.trees) {
            // 忽略树
            String tn = tree.getTreeName();
            if (!tree.hasItems() || !testTree.match(tn)) {
                continue;
            }
            // 循环树记录行
            for (WnDataSyncItem it : tree.getItems()) {
                // 忽略记录
                if (!testItem.match(it.getTestMap())) {
                    continue;
                }
                // 计数
                count++;
                // 跳过记录
                if (count <= skip) {
                    continue;
                }
                // 超限
                if (limit > 0 && count > limit) {
                    break;
                }
                // 输出
                String oldId = it.getBean().getString("id");
                String mapV = it.getPath();

                if (asId) {
                    WnObj o = it.loadObj(sys);
                    if (null == o) {
                        // 可能是路径问题，尝试格式化以后再加载
                        String ph = it.getPath().replaceAll("[(\\[\\])]", "");
                        String aph = Wn.normalizeFullPath(ph, sys);
                        System.out.printf("Fail to load obj: %s : retry : %s\n", it.toString(), aph);
                        o = sys.io.fetch(null, aph);
                    }
                    mapV = null == o ? null : o.id();
                }
                re.put(oldId, mapV);
            }
        }

        // 输出
        String json = Json.toJson(re, jfmt);
        sys.out.println(json);

    }

    protected WnMatch getItemMatch(ZParams params) {
        NutMap match = new NutMap();
        List<String> paths = new LinkedList<>();
        for (String val : params.vals) {
            // 文件还是目录
            if (val.matches("^(FILE|DIR)$")) {
                match.put("race", val);
            }
            // ~ 开始表示路径前缀
            else if (val.startsWith("~")) {
                if (!val.endsWith("*")) {
                    val = val + "*";
                }
                paths.add(val);
            }
            // 其他的可能是正则，可能是一个完整路径
            else {
                paths.add(val);
            }
        }
        // 记入路径过滤
        if (!paths.isEmpty()) {
            match.addv("path", paths);
        }
        // 构建匹配工具
        WnMatch testItem = AutoMatch.parse(match.isEmpty() ? null : match, true);
        return testItem;
    }

}
