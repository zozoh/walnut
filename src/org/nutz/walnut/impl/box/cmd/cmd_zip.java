package org.nutz.walnut.impl.box.cmd;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_zip extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "f");
        if (params.vals.length < 2) {
            throw Err.create("e.cmd.zip.miss_args");
        }

        String dest = params.vals[0];
        String[] srcArr = new String[params.vals.length - 1];
        for (int i = 1; i < params.vals.length; i++) {
            srcArr[i - 1] = params.vals[i];
        }

        String destPath = Wn.normalizeFullPath(dest, sys);
        WnObj destObj = sys.io.fetch(null, destPath);
        if (destObj != null && !params.is("f")) {
            throw Err.create("e.cmd.zip.dest.exist");
        }

        if (destObj != null && destObj.isDIR()) {
            throw Err.create("e.cmd.zip.dest.isdir");
        }

        // 目标文件
        destObj = sys.io.createIfNoExists(null, destPath, WnRace.FILE);
        // 计算要列出的要处理的对象
        List<WnObj> srclist = evalCandidateObjsNoEmpty(sys, srcArr, 0);
        try {
            OutputStream out = sys.io.getOutputStream(destObj, 0);
            ZipOutputStream zipOut = new ZipOutputStream(out);
            String entryName = null;
            for (int i = 0; i < srclist.size(); i++) {
                WnObj srcObj = srclist.get(i);
                if (destObj.id().equals(srcObj.id())) {
                    continue;
                }
                entryName = srcObj.name();
                // 创建Zip条目
                ZipEntry entry = new ZipEntry(entryName);
                zipOut.putNextEntry(entry);

                BufferedInputStream bis = new BufferedInputStream(sys.io.getInputStream(srcObj, 0));

                byte[] b = new byte[1024];

                while (bis.read(b, 0, 1024) != -1) {
                    zipOut.write(b, 0, 1024);
                }
                bis.close();
                zipOut.closeEntry();
            }
            zipOut.flush();
            zipOut.close();
        }
        catch (Exception e) {
            throw Err.create(e, "e.cmd.zip.err");
        }
    }

}
