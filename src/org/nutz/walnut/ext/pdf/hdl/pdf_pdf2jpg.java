package org.nutz.walnut.ext.pdf.hdl;

import java.io.File;
import java.io.FileInputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class pdf_pdf2jpg implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 检查一下是否存在 /usr/bin/convert 即ImageMagic
        String convertExec = "/usr/bin/convert";
        if (!new File(convertExec).exists()) {
            sys.err.print(convertExec);
            return;
        }
        // 获取源文件和目标目录
        String source = hc.params.val_check(0);
        String dst = hc.params.val_check(1);
        WnObj wobj = sys.io.check(null, Wn.normalizeFullPath(source, sys));
        String dir = "/tmp/pdf2xxx/" + R.UU32();
        File tmpDir = new File(dir);
        Files.createDirIfNoExists(tmpDir);
        // 将pdf输出到临时目录取
        File pdfTmp = new File(tmpDir + ".source.pdf");
        Files.write(pdfTmp, sys.io.getInputStream(wobj, 0));
        // 获取总页数
        PDDocument doc = PDDocument.load(sys.io.getInputStream(wobj, 0));
        int pageCount = doc.getNumberOfPages();
        doc.close();
        WnObj wdir = sys.io.createIfNoExists(null, Wn.normalizeFullPath(dst, sys), WnRace.DIR);
        try {
            // 逐页转换
            for (int i = 0; i < pageCount; i++) {
                String input = pdfTmp.getAbsolutePath() + "[" + i + "]";
                String imageName = "output_" + String.format("%03d", i) + ".jpg";
                String output = tmpDir.getAbsolutePath() + "/" + imageName;
                Lang.execOutput(new String[]{convertExec, input, output});
                WnObj tmp = sys.io.createIfNoExists(wdir, imageName, WnRace.FILE);
                try (FileInputStream ins = new FileInputStream(output)) {
                    sys.io.writeAndClose(tmp, ins);
                }
                new File(output).delete();
            }
        }
        finally {
            pdfTmp.delete();
        }
    }
}
