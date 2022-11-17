package org.nutz.walnut.ext.data.unzipx;

import java.io.InputStream;
import java.nio.charset.Charset;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterContext;
import org.nutz.walnut.util.archive.WnArchiveReadingCallback;
import org.nutz.walnut.util.archive.impl.WnZipArchiveReading;

public class UnzipxContext extends JvmFilterContext {

    public WnObj oZip;

    public Charset charset;
    public boolean hidden;
    public boolean macosx;

    public WnZipArchiveReading openReading(WnArchiveReadingCallback callback, Charset cs) {
        if (null == cs) {
            cs = this.charset;
        }
        // 准备输入流
        InputStream ins = sys.io.getInputStream(oZip, 0);

        WnZipArchiveReading ing = new WnZipArchiveReading(ins, cs);
        ing.onNext((en, zin) -> {
            // 判断一下隐藏文件
            if (!hidden && en.name.startsWith(".") || en.name.contains("/.")) {
                return;
            }
            // 判断一下 MACOS特殊文件夹
            if (!macosx && en.name.startsWith("__MACOSX") || en.name.contains("/__MACOSX")) {
                return;
            }
            callback.invoke(en, zin);
        });
        return ing;
    }

}
