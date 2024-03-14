package com.site0.walnut.ext.old.sync.hdl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Files;
import org.nutz.lang.LoopException;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs("o")
public class sync_2local implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        boolean overwrite = hc.params.is("o");
        String source = hc.params.val_check(0);
        String target = hc.params.val_check(1);
        source = Wn.normalizeFullPath(source, sys);
        target = Disks.normalize(target);
        WnObj src = sys.io.check(null, source);
        NutMap ctx = new NutMap();
        ctx.setv("count", new AtomicLong());
        export2local(new File(target), src, overwrite, sys, ctx);
        ctx.remove("count");
        if (!ctx.isEmpty()) {
            for (Map.Entry<String, Object> en : ctx.entrySet()) {
                sys.out.printlnf("%s - %s", en.getKey(), en.getValue());
            }
        }
        sys.out.println("done");
    }

    protected void export2local(File dst, WnObj src, boolean overwrite, WnSystem sys, NutMap ctx) throws IOException {
        long c = ctx.getAs("count", AtomicLong.class).incrementAndGet();
        if (c % 100 == 0) {
            sys.out.println("export " + c);
        }
        if (src.isFILE()) {
            dst = new File(dst, src.name());
            if (dst.exists()) {
                if (dst.isFile() && !overwrite) {
                    return;
                }
                else if (dst.isDirectory()) {
                    dst = new File(dst, src.name());
                }
            }
            Files.createFileIfNoExists(dst);
            try (OutputStream out = new FileOutputStream(dst)) {
                sys.io.readAndClose(src, out);
            }
            return;
        }
        WnQuery q = new WnQuery();
        q.setv("pid", src.id());
        q.setv("d0", src.d0());
        q.setv("d1", src.d1());
        File dstDir = new File(dst, src.name());
        sys.io.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj ele, int length) throws ExitLoop, ContinueLoop, LoopException {
                try {
                    export2local(dstDir, ele, overwrite, sys, ctx);
                }
                catch (Throwable e) {
                    ctx.setv("" + ele.path(), e.getMessage());
                }
            }
        });
    }
}
