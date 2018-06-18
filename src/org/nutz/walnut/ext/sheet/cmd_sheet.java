package org.nutz.walnut.ext.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
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
        // 字段映射
        SheetMapping mapping = new SheetMapping();
        String flds = params.get("flds");
        if (params.has("mapping")) {
            WnObj oMapping = Wn.checkObj(sys, params.get("mapping"));
            flds = sys.io.readText(oMapping);
        }
        mapping.parse(flds);
        List<NutMap> outputList = mapping.doMapping(inputList);

        // .................................................
        // 处理输出
        String fout = params.get("out");
        if (!Strings.isBlank(fout)) {
            String aph = Wn.normalizeFullPath(fout, sys);
            WnObj oOut = sys.io.createIfNoExists(null, aph, WnRace.FILE);
            String typeOutput = params.get("tpo", oOut.type());
            OutputStream ops = sys.io.getOutputStream(oOut, 0);
            wss.writeAndClose(ops, typeOutput, outputList, confOutput);
        }
        // 输入到标准输出
        else {
            String typeOutput = params.get("tpo", "csv");
            OutputStream ops = sys.out.getOutputStream();
            wss.writeAndClose(ops, typeOutput, outputList, confOutput);
        }

    }

}
