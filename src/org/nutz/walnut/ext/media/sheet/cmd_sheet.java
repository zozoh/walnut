package org.nutz.walnut.ext.media.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.bean.WnBeanMapping;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

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
        List<? extends NutBean> inputList;
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
        Object keysBy = params.get("keys");
        Object omitBy = params.get("omit");
        Object pickBy = params.get("pick");
        WnMatch keys = null;
        WnMatch omit = null;
        WnMatch pick = null;
        if (null != keysBy) {
            keys = AutoMatch.parse(keysBy);
        }
        if (null != omitBy) {
            omit = AutoMatch.parse(omitBy);
        }
        if (null != pickBy) {
            pick = AutoMatch.parse(pickBy);
        }
        // .................................................
        // 准备分页器
        int skip = params.getInt("skip", 0);
        int limit = params.getInt("limit", 0);
        // .................................................
        // 字段映射
        SheetMapping mapping = new SheetMapping();
        mapping.setKeys(keys);
        mapping.setOmit(omit);
        mapping.setPick(pick);
        mapping.setLimit(limit);
        mapping.setSkip(skip);
        // 解析映射字段
        String flds = params.get("flds");
        if (params.has("mapping")) {
            WnObj oMapping = Wn.checkObj(sys, params.get("mapping"));
            // 采用 BeanMapping 方式映射
            if (oMapping.isType("json")) {
                String json = sys.io.readText(oMapping);
                NutMap map = Json.fromJson(NutMap.class, json);
                WnBeanMapping bm = new WnBeanMapping();
                Map<String, NutMap[]> caches = new HashMap<>();
                NutMap vars = sys.session.getVars();
                bm.setFields(map, sys.io, vars, caches);
                mapping.setBeanMapping(bm);
            }
            // 默认的简易映射
            else {
                flds = sys.io.readText(oMapping);
                mapping.parse(flds);
            }
        }
        // 解析映射字段
        else {
            mapping.parse(flds);
        }

        // 执行映射
        List<NutBean> outputList = mapping.doMapping(inputList);

        // .................................................
        // 准备输出的键
        List<String> headKeys = mapping.getHeadKeys();

        // .................................................
        // 处理输出
        String fout = params.get("out");
        if (!Strings.isBlank(fout)) {
            // 因为输出到文件，所以可以指定想标准输出输出日志
            String process = params.get("process");
            // 处理自动process
            if ("<auto>".equalsIgnoreCase(process)) {
                process = "${P}";
                int N = Math.min(3, headKeys.size());
                for (int i = 0; i < N; i++) {
                    String k = headKeys.get(i);
                    process += String.format(": ${%s} ", k);
                }
            }
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
            wss.writeAndClose(ops, typeOutput, outputList, headKeys, confOutput, out, process);
        }
        // 输入到标准输出
        else {
            String typeOutput = params.get("tpo", "csv");
            OutputStream ops = sys.out.getOutputStream();
            wss.writeAndClose(ops, typeOutput, outputList, headKeys, confOutput);
        }

    }

}
