package com.site0.walnut.core.bm;

import java.io.File;

import com.site0.walnut.util.Wfiles;
import org.nutz.lang.random.R;

import com.site0.walnut.api.err.Er;

public class BMSwapFiles {

    public static BMSwapFiles create(String phSwap, boolean autoCreate) {
        File dSwap = new File(phSwap);
        if (!dSwap.exists()) {
            // 不自动创建，就自裁！！！
            if (!autoCreate) {
                throw Er.create("e.io.bm.local.SwapHomeNotFound", phSwap);
            }
            dSwap = Wfiles.createDirIfNoExists(phSwap);
        }
        // 不是目录，自裁
        if (!dSwap.isDirectory()) {
            throw Er.create("e.io.bm.local.SwapHomeMustBeDirectory", dSwap.getAbsolutePath());
        }
        return new BMSwapFiles(dSwap);
    }

    private File dSwap;

    public BMSwapFiles(File dSwap) {
        this.dSwap = dSwap;
    }

    public File createSwapFile() {
        String nm = R.UU32();
        File f = Wfiles.getFile(dSwap, nm);
        return Wfiles.createFileIfNoExists(f);
    }

    @Override
    public boolean equals(Object input) {
        if (this == input) {
            return true;
        }
        if (null == input || null == this.dSwap) {
            return false;
        }
        if (input instanceof BMSwapFiles) {
            File ta = ((BMSwapFiles) input).dSwap;
            return this.dSwap.equals(ta);
        }
        return false;
    }

}
