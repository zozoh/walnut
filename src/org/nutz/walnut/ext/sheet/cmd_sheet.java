package org.nutz.walnut.ext.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_sheet extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        // 准备服务类
        WnSheetService wss = new WnSheetService(sys.io);

        // 准备处理器配置参数
        NutMap confInput = params.getMap("ci", new NutMap());
        NutMap confOutput = params.getMap("co", new NutMap());
        
        if (confInput.containsKey("images")) {
        	confInput.put("images", Wn.normalizeFullPath(confInput.getString("images"), sys));
        }

        // .................................................
        // 读取输入
        List<NutMap> inputList;
        String fin = params.val(0);
        if (!Strings.isBlank(fin)) {
            WnObj oSheet = Wn.checkObj(sys, fin);
            String typeInput = params.get("tpi", oSheet.type());
            InputStream ins = sys.io.getInputStream(oSheet, 0);
            inputList = wss.readAndClose(ins, typeInput, confInput);
        }
        // 从标准输入
        else {
            InputStream ins = sys.in.getInputStream();
            String typeInput = params.get("tpi", "json");
            inputList = wss.readAndClose(ins, typeInput, confInput);
        }
        // .................................................
        // 准备过滤器
        NutMap filter = params.getMap("filter");
        NutMap matcher = params.getMap("match");
        if (null != filter)
            filter.evalSelfForMatching();
        if (null != matcher)
            matcher.evalSelfForMatching();
        // .................................................
        // 准备分页器
        int skip = params.getInt("skip", 0);
        int limit = params.getInt("limit", 0);
        // .................................................
        // 字段映射
        SheetMapping mapping = new SheetMapping();
        mapping.setFilter(filter);
        mapping.setMatcher(matcher);
        mapping.setLimit(limit);
        mapping.setSkip(skip);
        // 解析映射字段
        String flds = params.get("flds");
        if (params.has("mapping")) {
            WnObj oMapping = Wn.checkObj(sys, params.get("mapping"));
            flds = sys.io.readText(oMapping);
        }
        mapping.parse(flds);

        // 执行映射
        List<NutMap> outputList = mapping.doMapping(inputList);

        // .................................................
        // 处理输出
        String fout = params.get("out");
        if (!Strings.isBlank(fout)) {
            // 因为输出到文件，所以可以指定想标准输出输出日志
            String process = params.get("process");
            WnOutputable out = null;
            if (!Strings.isBlank(process)) {
                out = sys.out;
            } else {
                process = null;
            }

            String aph = Wn.normalizeFullPath(fout, sys);
            WnObj oOut = sys.io.createIfNoExists(null, aph, WnRace.FILE);
            String typeOutput = params.get("tpo", oOut.type());
            OutputStream ops = sys.io.getOutputStream(oOut, 0);
            wss.writeAndClose(ops, typeOutput, outputList, confOutput, out, process);
        }
        // 输入到标准输出
        else {
            String typeOutput = params.get("tpo", "csv");
            OutputStream ops = sys.out.getOutputStream();
            wss.writeAndClose(ops, typeOutput, outputList, confOutput);
        }

    }

}
